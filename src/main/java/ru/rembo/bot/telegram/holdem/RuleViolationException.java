package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.GlobalProperties;

import java.util.Locale;

public class RuleViolationException extends RuntimeException {

    Object[] args;

    public RuleViolationException(String message) {
        super(message);
    }

    public RuleViolationException(String message, Object... args) {
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
