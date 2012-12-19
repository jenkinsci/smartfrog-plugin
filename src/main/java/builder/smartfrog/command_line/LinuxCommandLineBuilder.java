package builder.smartfrog.command_line;

import builder.smartfrog.SmartFrogAction;
import builder.smartfrog.SmartFrogHost;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jcechace
 * Date: 8/13/12
 * Time: 1:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinuxCommandLineBuilder extends AbstractCommandLineBuilder implements CommandLineBuilder {

    public LinuxCommandLineBuilder(SmartFrogHost host) {
        super(host);
    }
    public  LinuxCommandLineBuilder(){

    }

    @Override
    public String getIniPath() {
        return getBuilder().isUseAltIni() ? getBuilder().getSfIni() : getSfInstance().getPath() + "/bin/default.ini";
    }

    @Override
    public String getSupportPath() {
        return getSfInstance().getSupport();
    }

    @Override
    public String getSfInstancePath() {
        return getSfInstance().getPath();
    }

    @Override
    public String getRunScript() {
        return getSupportPath() + "/runSF.sh";
    }

    @Override
    public String getStopScript() {
        return  getSupportPath() + "/stopSF.sh";
    }

    @Override
    public String getDeployScript() {
        return getSupportPath() + "/deploySF.sh";
    }

    @Override
    public String getWorkspacePath() {
        return getWorkspace();
    }

    @Override
    public String exportMatrixAxes(){
        String exportedMatrixAxes = " ";

        AbstractBuild<?, ?> build = getHost().getSfAction().getOwnerBuild();

        if (build.getProject() instanceof MatrixConfiguration){
            MatrixConfiguration matrix = (MatrixConfiguration) build.getProject();
            Combination combinations = matrix.getCombination();
            // Add only "SF_" prefixed variables.
            for (Map.Entry<String, String> entry : combinations.entrySet()) {
                if (entry.getKey().startsWith("SF_")) {
                    exportedMatrixAxes = exportedMatrixAxes + "export " + entry.getKey() + "=" + entry.getValue() + "; ";
                }
            }
        }
        return exportedMatrixAxes;
    }
}
