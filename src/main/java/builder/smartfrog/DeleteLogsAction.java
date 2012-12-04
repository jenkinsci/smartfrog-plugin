/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package builder.smartfrog;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import hudson.security.Permission;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author lucinka
 */
public class DeleteLogsAction implements Action{
    
    private AbstractBuild build;
    
    public DeleteLogsAction(AbstractBuild build){
        this.build = build;
    }

    public String getIconFileName() {
        return "plugin/smartfrog-plugin/icons/smartfrogDelete24.png";
    }

    public String getDisplayName() {
        return "delete smartforg logs";
    }

    public String getUrlName() {
        return "smartfrogLogsDelete";
    }
    
    public boolean isAvailable(){
        if(!build.isBuilding()){
            for(SmartFrogAction action: getSmartFrogActions()){
                if(action.getLogFile().exists())
                    return true;
            }
        }
        return false;
    }
    
    public Permission getPermission(){
        return Run.DELETE;
    }
    
    public AbstractBuild getBuild(){
        return build;
    }
    
    public List<SmartFrogAction> getSmartFrogActions(){
        return build.getActions(SmartFrogAction.class);
    }
    
    public void doConfirmDeleteSmartFrogLogs(StaplerRequest request, StaplerResponse response) throws ServletException, IOException{
        if (request==null)  return;
        String method = request.getMethod();
        if(!method.equalsIgnoreCase("POST"))
            throw new ServletException("Must be POST, Can't be "+method);
        build.getACL().checkPermission(getPermission());
        List<SmartFrogAction> actions = new ArrayList<SmartFrogAction>();
        for(SmartFrogAction action: getSmartFrogActions()){
            String param = request.getParameter(action.getHost() +"_"+action.getLogNum()); 
            if(param!=null){
                actions.add(action);
            }
        }
        request.setAttribute("actions", actions);
        request.getView(this, "confirm.jelly").forward(request, response);
        
    }
    
    public void doDeleteSmartFrogLogs(StaplerRequest request, StaplerResponse response) throws ServletException, IOException{
        if (request==null)  return;
        String method = request.getMethod();
        if(!method.equalsIgnoreCase("POST"))
            throw new ServletException("Must be POST, Can't be "+method);
        build.getACL().checkPermission(getPermission());
        StringBuffer buffer = request.getRequestURL();
        String url = buffer.toString();
         for(SmartFrogAction action: getSmartFrogActions()){
            if(request.getParameter(String.valueOf(action.getHost()+ "_"+ action.getLogNum()))!=null){
                File file = action.getLogFile();
                if(file.exists())
                    file.delete();
                if(file.exists()){
                  request.setAttribute("file", file);
                  request.getView(this, "error.jelly").forward(request, response);
                  return;
                }                   
                  
            }
        }
        response.forward(build, url.replaceAll("/smartfrogLogsDelete", ""), request);
        
    }
    
}
