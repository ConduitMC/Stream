package systems.conduit.stream.launcher;

import systems.conduit.stream.Constants;
import systems.conduit.stream.Logger;

import java.util.Arrays;

public class DebugLauncher {

    // TODO: Conduit launch without mixin json?
    public static void main(String... args) {
        Constants.DEBUG = true;
        LauncherStart.MIXINS.add("mixins.conduit.json");
        Logger.shouldUseLogger = true;
        if (args.length <= 0) {
            Logger.fatal("Specify a minecraft version as the first argument");
            return;
        }
        Constants.MINECRAFT_VERSION = args[0];
        LauncherStart.main(Arrays.copyOfRange(args, 1, args.length));
    }
}
