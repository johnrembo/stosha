package ru.rembo.bot.telegram.poker;

public class BadConditionException extends RuntimeException {
    public BadConditionException(String message) {
        super(message);
    }
}
