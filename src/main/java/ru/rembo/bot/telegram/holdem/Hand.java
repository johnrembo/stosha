package ru.rembo.bot.telegram.holdem;

import java.util.HashSet;

public class Hand extends HashSet<Card> {

    @Override
    public boolean add(Card card) {
        if (size() == 2) throw new BadConditionException("Already have two cards");
        return super.add(card);
    }
}
