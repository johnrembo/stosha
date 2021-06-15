package ru.rembo.bot.telegram.poker;

import java.util.*;

public class Deck {
    public LinkedHashSet<Card> cards = new LinkedHashSet<>();
    private boolean locked = false;
    private boolean shuffled = false;

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (rank != Rank.JOKER) {
                    Card card = new Card(suit, rank);
                    cards.add(card);
                }
            }
        }
    }

    public int cardCount() {
        return cards.size();
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void shuffle() {
        if (!locked) {
            List<Card> deck = new ArrayList<>(cards);
            Collections.shuffle(deck);
            cards = new LinkedHashSet<>(deck);
            shuffled = true;
        } else {
            throw new RuleViolationException("Deck is in game");
        }
    }

    @Override
    public String toString() {
        return "Deck{" +
                "cards=" + cards +
                '}';
    }

    public Card pullCard() {
        if (cards.isEmpty()) throw new BadConditionException("Deck is empty");
        if (!shuffled) throw new RuleViolationException("Deck is not shuffled");
        Card card = cards.iterator().next();
        cards.remove(card);
        locked = true;
        return card;
    }
}
