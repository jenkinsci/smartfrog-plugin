package builder.smartfrog.util;

import builder.smartfrog.SmartFrogHost;
import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author vjuranek
 *
 */
public class Functions {

    public static String convertWsToCanonicalPath(FilePath workspace){
        String workspacePath = "";
        try {
            workspacePath = (new File(workspace.toURI())).getCanonicalPath();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return workspacePath;
    }
    
    public static String cmdArrayToString(String[] cmds){
        StringBuilder buf = new StringBuilder();
        for (String c : cmds) {
            buf.append(c).append(" ");
        }
        return buf.substring(0, buf.length()-1);
    }

    public static List<SmartFrogHost> parseHosts(String string){
        String[] prep = string.split("[ \t]+");

        List<SmartFrogHost> sfHosts = new ArrayList<SmartFrogHost>();
        for (String p : prep){
            SmartFrogHost host = SmartFrogHost.fromString(p);
            if (host != null){
                sfHosts.add(host);
            }
        }
        return sfHosts;
    }
}
