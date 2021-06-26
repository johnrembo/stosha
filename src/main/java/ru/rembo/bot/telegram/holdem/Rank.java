package ru.rembo.bot.telegram.holdem;

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
    KING;


    @Override
    public String toString() {
        switch (this) {
            case ACE: return "\uD835\uDC00";
            case JACK: return "\uD835\uDC09";
            case QUEEN: return "\uD835\uDC10";
            case KING: return "\uD835\uDC0A";
            case TWO: return "\uD835\uDFD0";
            case THREE: return "\uD835\uDFD1";
            case FOUR: return "\uD835\uDFD2";
            case FIVE: return "\uD835\uDFD3";
            case SIX: return "\uD835\uDFD4";
            case SEVEN: return "\uD835\uDFD5";
            case EIGHT: return "\uD835\uDFD6";
            case NINE: return "\uD835\uDFD7";
            case TEN: return "\uD835\uDFCF\uD835\uDFCE";
            case JOKER: return "\uD83C\uDCCF";
            default: return String.valueOf(this.ordinal());
        }
    }
}

class CompareHiAce implements Comparator<Rank> {
    public int compare(Rank o1, Rank o2) {
        if (o1.ordinal() == 1) return 1;
        else if (o2.ordinal() == 1) return -1;
        else return o1.compareTo(o2);
    }
}

