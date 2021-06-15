package ru.rembo.bot.telegram;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global bot properties
 */

public class GlobalProperties {

    public static Properties properties = new Properties();
    private static final Logger consoleLog = Logger.getLogger(Properties.class.getName());
    private static boolean initialized = false;

    private static Properties getProperties() {
        if (!initialized) {
            consoleLog.setLevel(Level.ALL);
            consoleLog.info("Loading global config");
            try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    consoleLog.severe("Config not found");
                    throw new RuntimeException("SEVERE: Config not found");
                }
                properties.load(input);
            } catch (IOException e) {
                consoleLog.severe("Cannot access 'config.properties' file");
                throw new RuntimeException("SEVERE: Cannot read config", e);
            }
            consoleLog.info("Global config loaded");
            initialized = true;
        }
        return properties;
    }

    public static String get(String name) {
        return getProperties().getProperty(name);
    }

    public static void set(String name, String value) {
        getProperties().setProperty(name, value);
    }

}
