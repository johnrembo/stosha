package ru.rembo.bot.telegram.poker;

public class BadStateException extends RuntimeException {
    public BadStateException(String message) {
        super(message);
    }
}
