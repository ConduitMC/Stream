package systems.conduit.stream;

import org.apache.logging.log4j.LogManager;

public class Logger {

    protected static void info(boolean firstLaunch, String message) {
        if (firstLaunch) {
            System.out.println(message);
        } else {
            LogManager.getLogger(Constants.LOGGER_NAME).info(message);
        }
    }

    protected static void fatal(boolean firstLaunch, String message) {
        if (firstLaunch) {
            System.out.println(message);
        } else {
            LogManager.getLogger(Constants.LOGGER_NAME).fatal(message);
        }
    }
}
