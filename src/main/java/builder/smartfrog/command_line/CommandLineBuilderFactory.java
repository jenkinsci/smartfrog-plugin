package builder.smartfrog.command_line;

import builder.smartfrog.SmartFrogAction;
import builder.smartfrog.SmartFrogHost;

/**
 * Created with IntelliJ IDEA.
 * User: jcechace
 * Date: 8/13/12
 * Time: 1:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineBuilderFactory {
    public static CommandLineBuilder getInstance(SmartFrogHost host){
        SmartFrogHost.Platform platform = host.getPlatform();
        CommandLineBuilder commandLineBuilder = null;

        switch (platform){
            case LINUX:
                commandLineBuilder = new LinuxCommandLineBuilder(host);
            break;
            case SOLARIS:
                commandLineBuilder = new LinuxCommandLineBuilder(host);
            break;
            case WINDOWS:
                commandLineBuilder = new WindowsCommandLineBuilder(host);
            break;
            default: commandLineBuilder = new LinuxCommandLineBuilder(host);
        }

        return commandLineBuilder;
    }
}
