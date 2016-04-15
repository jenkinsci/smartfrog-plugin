/*
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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Lucie Votypkova
 */
@Extension
public class SmartFrogBuildListener extends RunListener<Run>{
    
    @Override
    public void onFinalized(Run r){
        final List<SmartFrogAction> actions = r.getActions(SmartFrogAction.class);
        if(!actions.isEmpty()){
            Computer.threadPoolForRemoting.submit(new Thread("Compression smartfrog logs of build " + r.getDisplayName() + " of job " + r.getParent().getDisplayName()) {
                public void run() {
                    for(SmartFrogAction sfAction :actions){
                        File log = sfAction.getLogFile();
                        compressLog(log, new File(log.getParentFile(),log.getName()+".gz"));
                    }
                }
            });
        }
    }   
    
    public void compressLog(File log, File compressedLog) {
        GZIPOutputStream gzos = null;
        FileInputStream ist = null;
        try{
            gzos = new GZIPOutputStream(new FileOutputStream(compressedLog));
            ist = new FileInputStream(log);
            IOUtils.copy(ist, gzos);
            gzos.finish();
        }
        catch(IOException e){
             Logger.getLogger(SmartFrogBuildListener.class.getName()).log(Level.WARNING, "was not able to compress log file " + log.getAbsolutePath(), e);
             if(ist!=null)
                 IOUtils.closeQuietly(ist);
             if(gzos!=null)
                 IOUtils.closeQuietly(gzos);
             return;
        }
        log.delete();
    }
}
