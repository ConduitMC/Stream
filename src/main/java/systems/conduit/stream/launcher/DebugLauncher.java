package systems.conduit.stream.launcher;

import systems.conduit.stream.Logger;

import java.util.Arrays;

public class DebugLauncher {

    // TODO: Conduit launch without mixin json?
    public static void main(String... args) {
        LauncherStart.debug = true;
        LauncherStart.MIXINS.add("mixins.conduit.json");
        Logger.shouldUseLogger = true;
        System.setProperty("log4j2.loggerContextFactory", "org.apache.logging.log4j.core.impl.Log4jContextFactory");
        if (args.length <= 0) {
            Logger.fatal("Specify a minecraft version as the first argument");
            return;
        }
        LauncherStart.minecraft_version = args[0];
        LauncherStart.main(Arrays.copyOfRange(args, 1, args.length));
    }
}
