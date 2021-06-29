package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.GlobalProperties;

public class BadConditionException extends RuntimeException {
    public BadConditionException(String message) {
        super(message);
    }

    public String getLocalizedMessage() {
        return GlobalProperties.getRandomException(super.getMessage(), GlobalProperties.defaultLocale);
    }
}
