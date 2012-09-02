package builder.smartfrog.command_line;

/**
 * Created with IntelliJ IDEA.
 * User: jcechace
 * Date: 8/13/12
 * Time: 1:29 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CommandLineBuilder {
    String[] buildDaemonCommandLine();
    String[] buildStopDaemonCommandLine();
    String[] buildDeployCommandLine(String scriptPath, String componentName);
}
