package ru.rembo.bot.telegram.holdem;

import org.jetbrains.annotations.NotNull;

public class Card implements Comparable<Rank> {
    public Suit suit;
    public Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "{" + rank + " of " + suit + "}";
    }

    @Override
    public int compareTo(@NotNull Rank o) {
        if ((rank.ordinal() == 1) && (o.ordinal() == 1)) {
            return 0;
        } else if (rank.ordinal() == 1) {
            return 1;
        } else if (o.ordinal() == 1) {
            return -1;
        } else {
            return rank.ordinal() - o.ordinal();
        }
    }

    public static <T> int getHiAceRank(T t) {
        return ((Card) t).rank.equals(Rank.ACE) ? 14 : ((Card) t).rank.ordinal();
    }
}
