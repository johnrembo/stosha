package ru.rembo.bot.telegram.statemachine;

import ru.rembo.bot.telegram.GlobalProperties;

import java.util.Locale;

public class BadStateException extends RuntimeException {

    Object[] args;

    public <T extends Enum<T>> BadStateException(T before, T after) {
        super(before.toString() + "->" + after.toString());
    }

    public BadStateException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public String getLocalizedMessage(Locale locale, Object... args) {
        return String.format(GlobalProperties.getRandomException(super.getMessage(), locale), args);
    }

    public String getLocalizedMessage(Locale locale) {
        return String.format(GlobalProperties.getRandomException(super.getMessage(), locale), args);
    }

    public String getLocalizedMessage() {
        return String.format(GlobalProperties.getRandomException(super.getMessage(), GlobalProperties.defaultLocale), args);
    }
}
