package com.demoing.app.core.utils;

import com.demoing.app.core.config.Configuration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * A little Logger class helper to output some basc log to console for debug purpose.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class Logger {
    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int FINED = 3;
    public static final int DETAILED = 4;
    public static final int ALL = 5;

    private static int logLevel = 0;

    private static DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static String entityLogFilterMatching;

    public Logger(Configuration c) {
        logLevel = c.logLevel;
        entityLogFilterMatching = c.logEntityFilterMatching;
    }

    /**
     * <p>Write a log message to the system out stream with a <code>level</code> of trace,
     * the <code>className</code> that emit the log and the <code>message</code> itself and its
     * arguments <code>args</code> (if necessary).</p>
     *
     * @param level     Level of logging 0=ERR to 5=DETAILS
     * @param className the class tha emitting the log message
     * @param message   the message to be output
     * @param args      arguments array to format the correct message.
     */
    public static void log(int level, Class className, String message, Object... args) {
        if (logLevel >= level && isEntityFiltered(args)) {
            ZonedDateTime ldt = ZonedDateTime.now();
            System.out.printf("[%s] %s : %s - %s\n", ldt.format(dtf), className, level, String.format(message, args));
        }
    }

    private static boolean isEntityFiltered(Object[] args) {
        return Logger.entityLogFilterMatching.equals("") || Arrays.stream(args).anyMatch(
                o -> o instanceof String
                        && Logger.entityLogFilterMatching.contains((String)o));
    }

    public static void setLevel(int level) {
        Logger.logLevel = level;
    }
}
