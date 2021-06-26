package ru.rembo.bot.telegram.statemachine;

import ru.rembo.bot.telegram.GlobalProperties;

import java.util.Locale;

public class BadStateException extends RuntimeException {

    private Object after;
    private Object before;

    public BadStateException(String message) {
        super(message);
    }

    public <T extends Enum<T>> BadStateException(T before, T after) {
        super(before.toString() + "->" + after.toString());
        this.before = before;
        this.after = after;
    }

    public Object getBefore() {
        return before;
    }

    public Object getAfter() {
        return after;
    }

    public String getLocalizedMessage(Locale locale) {
        return GlobalProperties.getRandomException(super.getMessage(), locale);
    }
}
