package builder.smartfrog.command_line;

import builder.smartfrog.SmartFrogHost;

/**
 * @author jcechace
 */

// TODO: Implemet this
public class WindowsCommandLineBuilder extends AbstractCommandLineBuilder implements CommandLineBuilder {

    public WindowsCommandLineBuilder(SmartFrogHost host) {
        super(host);
        throw new RuntimeException("Not implemented yet");
    }

    public String[] buildDaemonCommandLine() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getIniPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getSupportPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getSfInstancePath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getRunScript() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getStopScript() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getDeployScript() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getWorkspacePath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String exportMatrixAxes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] buildStopDaemonCommandLine(String host) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
