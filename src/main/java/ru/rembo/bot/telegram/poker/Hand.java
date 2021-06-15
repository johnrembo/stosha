package ru.rembo.bot.telegram.poker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Hand extends HashSet<Card> {

    public Card getFirst() {
        if (size() < 1) throw new BadConditionException("No first card");
        List<Card> list = new ArrayList<>(this);
        return list.get(0);
    }

    public Card getSecond() {
        if (size() < 2) throw new BadConditionException("No second card");
        List<Card> list = new ArrayList<>(this);
        return list.get(1);
    }

    @Override
    public boolean add(Card card) {
        if (size() == 2) throw new BadConditionException("Already have two cards");
        return super.add(card);
    }
}
