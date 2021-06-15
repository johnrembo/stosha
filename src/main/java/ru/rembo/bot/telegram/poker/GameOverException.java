package ru.rembo.bot.telegram.poker;

public class GameOverException extends RuntimeException {
    public GameOverException(String message) {
        super(message);
    }
}
