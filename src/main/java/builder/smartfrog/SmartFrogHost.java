package builder.smartfrog;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.util.ListBoxModel;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author jcechace
 */
public class SmartFrogHost extends AbstractDescribableImpl<SmartFrogHost> {
    public static final Platform DEF_PLATFORM = Platform.LINUX;

    private SmartFrogAction sfAction;
    private String name;
    private Platform platform = DEF_PLATFORM;
    private String jdk;
    private String sfOpts;

    /*
    * Factory method
    */
    public static SmartFrogHost fromString(String string){
        String[] prep = string.split("[|]");

        if (prep[0].length() == 0){
            return null;
        }

        SmartFrogHost host = new SmartFrogHost();
        host.setName(prep[0]);


        if (prep.length == 1){
            host.setPlatform(DEF_PLATFORM);
        }  else if (prep.length == 2){
            host.setPlatform(Platform.valueOf(prep[1].toUpperCase()));
        } else {
            host.setPlatform(Platform.valueOf(prep[1].toUpperCase()));
            host.setJdk(prep[2]);
        }

        return host;
    }

    /*
    * Host properties
    */
    public void setSfAction(SmartFrogAction sfAction){
        this.sfAction = sfAction;
    }
    public SmartFrogAction getSfAction() {
        return sfAction;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setPlatform(Platform platform){
        this.platform = platform;
    }
    public Platform getPlatform() {
        return platform;
    }

    public void setJdk(String jdk){
        this.jdk = jdk;
    }
    public String getJdk() {
        return jdk;
    }


    /*
    * Host specific daemon properties
    */
    public String getSfOpts() {
        return sfOpts;
    }
    public void setSfOpts(String sfOpts) {
        this.sfOpts = sfOpts;
    }


    public SmartFrogHost(){}

    @DataBoundConstructor
    public SmartFrogHost(String name, String platform, String jdk, String sfOpts){
        this.name = name;
        this.platform = Platform.valueOf(platform.toUpperCase());
        this.jdk = jdk;
        this.sfOpts = sfOpts;
    }

    public String toString(){
        return name;
    }

    /*
    * Supported platforms
    */
    public static enum Platform {
        LINUX,
        WINDOWS,
        SOLARIS,
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SmartFrogHost> {
        private List<Platform> platforms = new ArrayList<Platform>();

        @Override
        public String getDisplayName() {
            return "Machine on which will be started SmartFrog daemon";
        }

        public ListBoxModel doFillPlatformItems(){
            ListBoxModel lb = new ListBoxModel();
            for (Platform p : Platform.values()){
                lb.add(p.toString(), p.toString());
            }
            return lb;
        }

        public ListBoxModel doFillJdkItems(){
            ListBoxModel lb = new ListBoxModel();

            //TODO: find how to do this neater.
            new JDK.DescriptorImpl();
            JDK.DescriptorImpl descriptor = (JDK.DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(JDK.class);
            JDK[] jdks = descriptor.getInstallations();

            for (JDK jdk : jdks){
                lb.add(jdk.getName(),jdk.getHome());
            }
            return lb;
        }
    }
}
