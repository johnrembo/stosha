package ru.rembo.bot.telegram.poker;

import java.util.Comparator;

public enum Rank {
    JOKER,
    ACE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING
}

class CompareHiAce implements Comparator<Rank> {
    public int compare(Rank o1, Rank o2) {
        if (o1.ordinal() == 1) return 1;
        else if (o2.ordinal() == 1) return -1;
        else return o1.compareTo(o2);
    }
}

