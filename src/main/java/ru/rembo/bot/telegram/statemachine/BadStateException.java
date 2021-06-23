package ru.rembo.bot.telegram.statemachine;

public class BadStateException extends RuntimeException {
    public BadStateException(String message) {
        super(message);
    }
}
