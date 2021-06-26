package ru.rembo.bot.telegram.holdem;

public enum Suit {
    CLUBS,
    DIAMONDS,
    SPADES,
    HEARTS;


    @Override
    public String toString() {
        switch (this) {
            case CLUBS: return "♣️";
            case DIAMONDS: return "♦️";
            case SPADES: return "♠️";
            case HEARTS: return "♥️";
        }
        return super.toString();
    }
}
