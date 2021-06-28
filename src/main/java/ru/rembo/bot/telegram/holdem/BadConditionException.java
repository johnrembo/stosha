package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.GlobalProperties;

import java.util.Locale;

public class BadConditionException extends RuntimeException {
    public BadConditionException(String message) {
        super(message);
    }

    public String getLocalizedMessage(Locale locale) {
        return GlobalProperties.getRandomException(super.getMessage(), locale);
    }

    public String getLocalizedMessage() {
        return GlobalProperties.getRandomException(super.getMessage(), GlobalProperties.defaultLocale);
    }
}
