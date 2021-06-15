package ru.rembo.bot.telegram;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.*;

/**
 * Global bot logger
 *
 * Referenced @see <a href="https://github.com/rubenlagus/TelegramBotsExample/blob/master/src/main/java/org/telegram/Main.java">'BotLogger' class</a>
 * not found in Maven Telegram API package. So here we are.
 */

public class GlobalLogger {
    private static final Logger logger = Logger.getLogger(GlobalLogger.class.getName());
    private static boolean initialized = false;

    private static Logger getLogger() {
        if (!initialized) {
/*            Handler consoleLog = new ConsoleHandler();
            logger.addHandler(consoleLog);*/
            logger.setLevel(Level.parse(GlobalProperties.get("logLevelConsole")));
            logger.info("Loading global log");
            SimpleDateFormat dateYYYYMMDD = new SimpleDateFormat(GlobalProperties.get("logFileNameFormat"));
            String logFile = GlobalProperties.get("logDir") + File.separator +
                    GlobalProperties.get("logFileNamePrefix") +
                    dateYYYYMMDD.format(new Date()) + GlobalProperties.get("logFileNameSuffix");
            logger.info("Start logging to '" + logFile + "'");
            try {
                Handler fileHandler = new FileHandler(logFile, true);
                fileHandler.setFormatter(new SimpleFormatter() {
                    private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

                    @Override
                    public synchronized String format(LogRecord lr) {
                        return String.format(format,
                                new Date(lr.getMillis()),
                                lr.getLevel().getLocalizedName(),
                                lr.getMessage()
                        );
                    }
                });
                fileHandler.setLevel(Level.parse(GlobalProperties.get("logLevelFile")));
                logger.addHandler(fileHandler);
                logger.info("Log started with log level " + fileHandler.getLevel());
            } catch (IOException e) {
                logger.warning("Cannot access log file '" + logFile + "'");
                logger.info("Logging to console");
            } finally {
                initialized = true;
            }
        }
        return logger;
    }

    public static void log(Level level, String msg) {
        getLogger().log(level, msg);

    }

    public static void log(Level level, String msg, Throwable ex) {
        getLogger().log(level, msg, ex);

    }

    public static void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    public static void warning(String msg) {
        log(Level.WARNING, msg);
    }

    public static void warning(String msg, Exception e) {
        log(Level.WARNING, msg, e);
    }

    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void fine(String msg) {
        log(Level.FINE, msg);
    }

    public static void fine(String msg, Exception e) {
        log(Level.FINE, msg, e);
    }

    public static void finer(String msg) {
        log(Level.FINER, msg);
    }
}
