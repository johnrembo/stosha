package ru.rembo.bot.telegram.poker;

import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Combo implements Comparable<Combo> {
    Combination name;
    Card highCard;
    ArrayList<Card> hand;
    int rank;

    public Combo(Combination name, Card card, ArrayList<Card> hand) {
        this.name = name;
        this.highCard = card;
        this.hand = hand;
        this.rank = name.ordinal() * 1000 + ((card.rank.equals(Rank.ACE) ? 14 : card.rank.ordinal())) * 10;
    }


    public static <T> int compare(T o, T o2) {
        return ((Combo) o).compareTo((Combo) o2);
    }

    @Override
    public int compareTo(@NotNull Combo o) {
        return rank - o.rank;
    }
}
