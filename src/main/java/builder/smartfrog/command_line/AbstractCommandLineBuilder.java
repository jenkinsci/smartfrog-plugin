package builder.smartfrog.command_line;

import builder.smartfrog.SmartFrogAction;
import builder.smartfrog.SmartFrogBuilder;
import builder.smartfrog.SmartFrogHost;
import builder.smartfrog.SmartFrogInstance;
import builder.smartfrog.util.Functions;
import hudson.model.JDK;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jcechace
 * Date: 8/13/12
 * Time: 1:37 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCommandLineBuilder {
    public static final String DEF_JDK = "$JAVA_HOME";

    private SmartFrogBuilder builder;
    private SmartFrogInstance sfInstance;
    private SmartFrogHost host;
    private String workspace;




    public AbstractCommandLineBuilder(SmartFrogHost host){
        this.builder = host.getSfAction().getBuilder();
        this.sfInstance = this.builder.getSfInstance();
        this.host = host;
        this.workspace = Functions.convertWsToCanonicalPath(host.getSfAction().getOwnerBuild().getWorkspace());

    }

    public AbstractCommandLineBuilder(){

    }

    public SmartFrogBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(SmartFrogBuilder builder) {
        this.builder = builder;
    }

    public SmartFrogInstance getSfInstance() {
        return sfInstance;
    }

    public void setSfInstance(SmartFrogInstance sfInstance) {
        this.sfInstance = sfInstance;
    }

    public SmartFrogHost getHost() {
        return host;
    }

    public void setHost(SmartFrogHost host) {
        this.host = host;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getSfOpts(){
        return host.getSfOpts() == null ? getBuilder().getSfOpts() : host.getSfOpts();
    }

    public String getJdk(){
        // TODO: this can be improved by using getHost().getSfAction().getOwnerBuild().getEnvironment()
        JDK jdk = getHost().getSfAction().getOwnerBuild().getProject().getJDK();
        String path =  jdk == null ?  DEF_JDK : jdk.getHome();
        // TODO: Hack to prevent jenkins from expanding env variables
        path = path.replace("$", "@");
        return host.getJdk() != null ? host.getJdk() : path;
    }

    public String[] buildDaemonCommandLine() {
        // TODO: fix SfUSerHomeX
        SmartFrogBuilder builder = getBuilder();
        return new String[] { "bash", "-xe", getRunScript(),  getHost().getName(), getSfInstancePath(),
                builder.getSfUserHome(), getSupportPath(), builder.getSfUserHome2(), builder.getSfUserHome3(),
                builder.getSfUserHome4(), getWorkspacePath(), getSfOpts(), getIniPath(), exportMatrixAxes(), getJdk()};
    }


    public String[] buildStopDaemonCommandLine() {
        return new String[] { "bash", "-xe", getStopScript(), getHost().getName(), getSfInstancePath(),
                getBuilder().getSfUserHome(), getJdk()};
    }

    public String[] buildDeployCommandLine(String scriptPath, String componentName) {
        // TODO: fix SfUSerHomeX
        SmartFrogBuilder builder = getBuilder();
        return new String[] { "bash", "-xe", getDeployScript(), getHost().getName(), getSfInstancePath(),
                builder.getSfUserHome(), getSupportPath(), builder.getSfUserHome2(), builder.getSfUserHome3(),
                builder.getSfUserHome4(), scriptPath, componentName, getWorkspacePath(), exportMatrixAxes(), getJdk()};
    }

    abstract String getIniPath();
    abstract String getSupportPath();
    abstract String getSfInstancePath();
    abstract String getRunScript();
    abstract String getStopScript();
    abstract String getDeployScript();
    abstract String getWorkspacePath();
    abstract String exportMatrixAxes();
}
