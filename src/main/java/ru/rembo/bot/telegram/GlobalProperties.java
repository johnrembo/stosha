package ru.rembo.bot.telegram;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global bot properties
 */

public class GlobalProperties {

    public static Properties properties = new Properties();
    private static final Logger consoleLog = Logger.getLogger(Properties.class.getName());
    private static boolean initialized = false;
    public static final Locale defaultLocale = Locale.getDefault();
    public static final Locale enUSLocale = new Locale("en", "US");
    public static final Locale ruRULocale = new Locale("ru", "RU");
    public static final HashSet<Locale> supportedLocales = new HashSet<>();
    public static final HashMap<Locale, ResourceBundle> inputPatterns = new HashMap<>();
    public static final HashMap<Locale, ResourceBundle> outputMessages = new HashMap<>();
    public static final HashMap<Locale, ResourceBundle> exceptionMessages = new HashMap<>();

    public static class InputPatternMatch {

        public Locale locale;
        public String key;
        public HashMap<String, String> args;

        InputPatternMatch(Locale locale, String key, HashMap<String, String> args) {
            this.locale = locale;
            this.key = key;
            this.args = args;
        }

    }

    static {
        supportedLocales.add(enUSLocale);
        supportedLocales.add(ruRULocale);
    }

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

            consoleLog.info("Loading locales");
            for (Locale locale : supportedLocales) {
                inputPatterns.put(locale, ResourceBundle.getBundle("InputPatternsBundle", locale));
                outputMessages.put(locale, ResourceBundle.getBundle("OutputMessagesBundle", locale));
                exceptionMessages.put(locale, ResourceBundle.getBundle("ExceptionMessages", locale));
                consoleLog.info(locale.getDisplayName() + " loaded");
            }
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

    public static InputPatternMatch matchInput(Message message) {
        String key = null;
        HashMap<String, String> args = new HashMap<>();
        Locale locale = null;
        for (Map.Entry<Locale, ResourceBundle> entry : inputPatterns.entrySet()) {
            locale = entry.getKey();
            Enumeration<String> keys = entry.getValue().getKeys();
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                String[] patterns = entry.getValue().getString(key).split(";");
                for (String pattern : patterns) {
                    Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message.getText());
                    if (matcher.find()) {
                        Matcher namedGroups = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(pattern);
                        while (namedGroups.find()) {
                            args.put(namedGroups.group(1), matcher.group(namedGroups.group(1)));
                        }
                        args.put("0", matcher.group());
                        for (int i = 0; i < matcher.groupCount(); i++) {
                            if (!matcher.group(i).isEmpty()) {
                                args.put(String.valueOf(i), matcher.group(i));
                            }
                        }
                        break;
                    }
                }
                if (args.size() > 0) break;
            }
            if (args.size() > 0) break;
        }
        if (args.size() > 0) {
            return new InputPatternMatch(locale, key, args);
        } else {
            return null;
        }
    }

    public static String getExceptionMessage(String message, Locale locale) {
        if (exceptionMessages.get(locale).containsKey(message)) {
            String[] messages = exceptionMessages.get(locale).getString(message).split(";");
            return messages[(int) (Math.random() * (Arrays.stream(messages).count() - 1))];
        } else {
            return "Localized message not found for: " + message;
        }
    }

}
