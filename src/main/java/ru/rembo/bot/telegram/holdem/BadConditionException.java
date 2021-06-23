package ru.rembo.bot.telegram.holdem;

public class BadConditionException extends RuntimeException {
    public BadConditionException(String message) {
        super(message);
    }
}
