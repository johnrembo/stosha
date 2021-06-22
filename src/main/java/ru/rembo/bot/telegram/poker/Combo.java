package ru.rembo.bot.telegram.poker;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Combo implements Comparable<Combo> {
    public Combination name;
    public Card highCard;
    public ArrayList<Card> hand;
    public int rank;
    public ArrayList<Card> kickers = new ArrayList<>();

    public Combo(Combination name, Card card, ArrayList<Card> hand) {
        this.name = name;
        this.highCard = card;
        this.hand = hand;
        this.rank = name.ordinal() * 1000 + ((card.rank.equals(Rank.ACE) ? 14 : card.rank.ordinal())) * 10;
    }

    @Override
    public int compareTo(@NotNull Combo o) {
        return rank - o.rank;
    }

    public void setKicker(Card card) {
        this.rank = this.rank + card.rank.ordinal();
    }

    public void add(Combo smallPair) {
        this.hand.addAll(smallPair.hand);
        this.rank += ((smallPair.highCard.rank.equals(Rank.ACE) ? 14 : smallPair.highCard.rank.ordinal())) * 10;
    }
}
