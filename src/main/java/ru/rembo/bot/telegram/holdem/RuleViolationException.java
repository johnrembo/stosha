package ru.rembo.bot.telegram.holdem;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String message) {
        super(message);
    }
}
