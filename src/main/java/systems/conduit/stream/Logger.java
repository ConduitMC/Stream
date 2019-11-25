package systems.conduit.stream;

import org.apache.logging.log4j.LogManager;

public class Logger {

    public static boolean shouldUseLogger = false;

    public static void info(String message) {
        if (shouldUseLogger && canUseLogger()) {
            LogManager.getLogger(Constants.LOGGER_NAME).info(message);
        } else {
            System.out.println(message);
        }
    }

    public static void fatal(String message) {
        if (shouldUseLogger && canUseLogger()) {
            LogManager.getLogger(Constants.LOGGER_NAME).fatal(message);
        } else {
            System.out.println(message);
        }
    }

    private static boolean canUseLogger() {
        try {
            Class.forName("org.apache.logging.log4j.core.Logger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
