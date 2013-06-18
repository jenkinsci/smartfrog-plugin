/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package builder.smartfrog;

import builder.smartfrog.command_line.CommandLineBuilder;
import builder.smartfrog.command_line.CommandLineBuilderFactory;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.StreamBuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.LargeText;
import org.kohsuke.stapler.interceptor.RequirePOST;

import builder.smartfrog.util.ConsoleLogger;
import builder.smartfrog.util.Functions;
import builder.smartfrog.util.LineFilterOutputStream;

/**
 * 
 * @author Dominik Pospisil
 * @author vjuranek
 */
public class SmartFrogAction implements Action, Runnable {

    private static final String NL = System.getProperty("line.separator");

    private SmartFrogHost sfHost;
    private String host;
    private State state;
    private AbstractBuild<?, ?> build;
    private String prefixId;

    private transient SmartFrogBuilder builder;
    private transient Proc proc;
    private transient Thread execThread;
    private transient Vector<SmartFrogActionListener> listeners = new Vector<SmartFrogActionListener>();
    private transient Launcher launcher;
    private transient ConsoleLogger console;
    private transient BuildListener log;
    private transient String readableLogSize;

    public SmartFrogAction(SmartFrogBuilder builder, String host) {
        this.builder = builder;
        this.host = host;
        this.state = State.STARTING;
        this.prefixId = "";
    }
    
    public SmartFrogAction(SmartFrogBuilder builder, SmartFrogHost host, String prefixId) {
        this.builder = builder;
        this.sfHost = host;
        this.host = sfHost.getName();
        this.state = State.STARTING;
        this.prefixId = prefixId;
    }
    
    protected Object readResolve(){
        if(prefixId == null)
            prefixId = "";
        if (this.sfHost == null)
            this.sfHost = SmartFrogHost.fromString(this.host);
        return this;
    }

    public SmartFrogHost getSfHost() {
        return sfHost;
    }
    
    public String getReadableLogSize() {
        if(readableLogSize == null)
            readableLogSize = hudson.Functions.humanReadableByteSize(getLogFile().length());
        return readableLogSize;
    }


    public SmartFrogBuilder getBuilder(){
        return builder;
    }

    public void perform(final AbstractBuild<?, ?> build, final Launcher launcher, final ConsoleLogger console) throws IOException,
            InterruptedException {
        this.build = build;
        this.launcher = launcher;
        this.console = console;

        CommandLineBuilder commandLineBuilder = CommandLineBuilderFactory.getInstance(this.getSfHost());

        String[] cl = commandLineBuilder.buildDaemonCommandLine();
        logUpstream("[SmartFrog] INFO: Starting daemon on sfHost " + sfHost);
        logUpstream("[SmartFrog] INFO: Start command is " + Functions.cmdArrayToString(cl));
        log = new StreamBuildListener(new PrintStream(new SFFilterOutputStream(new FileOutputStream(getLogFile()))),
                Charset.defaultCharset());
        proc = launcher.launch().cmds(cl).envs(build.getEnvironment(log)).pwd(build.getWorkspace()).stdout(log).start();
        execThread = new Thread(this, "SFDaemon - " + sfHost);
        execThread.start();
    }

    public void run() {
        // wait for process to finish
        int status = 1; //by default fail
        try {
            if(proc.isAlive())
                status = proc.join();
        } catch (IOException ex) {
            status = 1;
            setState(State.FAILED);
        } catch (InterruptedException ex) {
            status = 1;
            setState(State.FAILED);
        } finally {
            //TODO reliable kill here  - JBQA 2006
            log.getLogger().close();
        }
        if(status != 0){
            logUpstream("[SmartFrog] INFO: Daemon on sfHost " + sfHost + " failed");
            setState(State.FAILED);
            return;
        }
        logUpstream("[SmartFrog] INFO: Daemon on sfHost " + sfHost + " finished");
        setState(State.FINISHED);
    }

    public void interrupt() {
        CommandLineBuilder commandLineBuilder = CommandLineBuilderFactory.getInstance(this.getSfHost());

        String[] cl = commandLineBuilder.buildStopDaemonCommandLine();

        logUpstream("[SmartFrog] INFO: Trying to interrupt daemon on sfHost " + sfHost);
        logUpstream("[SmartFrog] INFO: Interrupt command is " + Functions.cmdArrayToString(cl));
        try {
            //TODO possible concurrent writing into log (from interrupt() as well as from run())!! (however synchronization could lead to livelock)
            Proc killProc = launcher.launch().cmds(cl).envs(build.getEnvironment(log)).pwd(build.getWorkspace()).stdout(log).start();
            int exitCode = killProc.joinWithTimeout(5, TimeUnit.MINUTES, console.getListener());
            if(exitCode != 0) { 
                // something went wrong, let's try hard kill, also with timeout as whole machine can be unresponsive
                // TODO replace with something more sophisticated/universal than bash script
                String[] cmd = builder.buildKilleThemAllCommandLine(host);    
                Proc killThemAll = launcher.launch().cmds(cmd).envs(build.getEnvironment(log)).pwd(build.getWorkspace()).stdout(log).start();
                // still can take some time if e.g. server is under heavy load due to deadlock or something like this, so let wait few minutes too
                exitCode = killProc.joinWithTimeout(3, TimeUnit.MINUTES, console.getListener());
                if(exitCode != 0 && killThemAll.isAlive()) {
                    logUpstream("[SmartFrog] ERROR: Hard kill timeout, giving up. Machine is probably unresponsive, plase check it manually.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public State getState() {
        return state;
    }
    
    private void setState(State s) {
        if (this.getState() == s)
            return;
        this.state = s;
        logUpstream("[SmartFrog] INFO: Deamon on sfHost " + sfHost + " has changed state to " + state.toString());
        for (SmartFrogActionListener l : listeners)
            l.stateChanged();
    }

    public void addStateListener(SmartFrogActionListener l) {
        listeners.add(l);
    }

    public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
        new LargeText(getLogFile(), !isBuilding()).doProgressText(req, rsp);
    }
    
    public File getLogFile() {
        String logFileName = host;
        if(!prefixId.isEmpty())
            logFileName = prefixId + "_" +logFileName;
        
        File gzipLogFile = new File(build.getRootDir(), logFileName + ".log.gz");
        if(gzipLogFile.isFile())
            return gzipLogFile;
        return new File(build.getRootDir(), logFileName + ".log");
    }
    
    private void logUpstream(String message){
        console.logAnnot(message);
        //console.log(message);
    }
    
    public boolean isBuilding() {
        return (state != State.FAILED) && (state != State.FINISHED);
    }

    public String getIconFileName() {
        return "/plugin/smartfrog-plugin/icons/smartfrog24.png";
    }

    public String getDisplayName() {
        if(prefixId.isEmpty())
            return "sfDaemon - " + host;
        return "sfDaemon - " + host + " (" + prefixId + ")" ;
    }

    public String getUrlName() {
        if(prefixId.isEmpty())
            return "console-" + Util.rawEncode(host);
        return prefixId + "-console-" + Util.rawEncode(host);
    }

    // required by index.jelly
    public AbstractBuild<?,?> getOwnerBuild(){
        return build;
    }
    
    // required by consoleText.jelly
    public Reader getLogReader() throws IOException {
        return new InputStreamReader(getLogInputStream());
    }
    
    public InputStream getLogInputStream() throws IOException {
        File logFile = getLogFile();
        
        if (logFile != null && logFile.exists() ) {
            FileInputStream fis = new FileInputStream(logFile);
            if (logFile.getName().endsWith(".gz")) {
                return new GZIPInputStream(fis);
            } else {
                return fis;
            }
        }
        
        String message = "No such file: " + logFile;
        return new ByteArrayInputStream(message.getBytes());
    }
    
    @RequirePOST
    public void doDoDelete( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        build.checkPermission(Run.DELETE);
        File log = getLogFile();
        if(log != null)
            if(log.delete()) {
                build.getActions().remove(this);
                build.save();
            }
        rsp.sendRedirect2(req.getContextPath()+'/' + build.getUrl());
    }
    
    private class SFFilterOutputStream extends LineFilterOutputStream {

        private OutputStreamWriter os;

        public SFFilterOutputStream(OutputStream out) {
            super(out);
            os = new OutputStreamWriter(out);
        }

        protected void writeLine(String line) {

            if (line.startsWith("SmartFrog ready"))
                setState(State.RUNNING);

            int idx = line.indexOf("[TerminateHook]");
            if (idx > -1) {
                String compName = line.substring(line.indexOf('[', idx + 15) + 1);
                compName = compName.substring(0, compName.indexOf(']'));
                if (compName.endsWith(builder.getSfScriptSource().getScriptName())) {
                    //TODO keep this info locally?
                    builder.componentTerminated(!line.contains("ABNORMAL"));
                }
            }

            try {
                os.write(line);
                os.write(NL);
                os.flush();
            } catch (IOException ioe) {

            }
        }
    }

    public enum State {
        STARTING, RUNNING, FINISHED, FAILED
    };
}
