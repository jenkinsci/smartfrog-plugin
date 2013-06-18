package builder.smartfrog.command_line;

import builder.smartfrog.SmartFrogHost;

/**
 * @author jcechace
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
