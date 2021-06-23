package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.statemachine.*;

import java.util.*;

public class Deck extends AbstractActor<DeckState> {
    private LinkedHashSet<Card> cards = new LinkedHashSet<>();
    private final Collection<Card> discarded = new HashSet<>();
    private Collection<Card> disposal;
    private Card card;

    public int size() {
        return cards.size();
    }

    public static class DeckTransition extends AbstractTransition<DeckState> {
        DeckTransition(DeckState before, DeckState after) {
            super(before, after);
        }
    }

    public static class DeckActionMap extends AbstractActionMap<DeckState> {
        DeckActionMap(ActorBehaviour<DeckState> actorBehaviour) {
            super(actorBehaviour);
        }

        @Override
        public void init(Behaviour<DeckState> behaviour) {
            // static behaviour
        }
    }

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (rank != Rank.JOKER) {
                    Card card = new Card(suit, rank);
                    cards.add(card);
                }
            }
        }
        initState(DeckState.NEW);
        DeckActionMap actionMap = new DeckActionMap(this);
        actionMap.put(new DeckTransition(DeckState.NEW, DeckState.SHUFFLED), this::shuffle);
        actionMap.put(new DeckTransition(DeckState.NEW, DeckState.FULL), this::accept);
        actionMap.put(new DeckTransition(DeckState.FULL, DeckState.SHUFFLED), this::shuffle);
        actionMap.put(new DeckTransition(DeckState.SHUFFLED, DeckState.PULL_CARD), this::pullCard);
        actionMap.put(new DeckTransition(DeckState.PLAYED, DeckState.PULL_CARD), this::pullCard);
        actionMap.put(new DeckTransition(DeckState.PULL_CARD, DeckState.PULL_CARD), this::pullCard);
        actionMap.put(new DeckTransition(DeckState.PULL_CARD, DeckState.PLAYED), this::accept);
        actionMap.put(new DeckTransition(DeckState.PULL_CARD, DeckState.DISCARD), this::discard);
        actionMap.put(new DeckTransition(DeckState.PLAYED, DeckState.DISCARD), this::discard);
        actionMap.put(new DeckTransition(DeckState.DISCARD, DeckState.DISCARD), this::discard);
        actionMap.put(new DeckTransition(DeckState.DISCARD, DeckState.PLAYED), this::accept);
        actionMap.put(new DeckTransition(DeckState.DISCARD, DeckState.PULL_CARD), this::pullCard);
        actionMap.put(new DeckTransition(DeckState.PLAYED, DeckState.FULL), this::restore);
        actionMap.put(new DeckTransition(DeckState.DISCARD, DeckState.FULL), this::restore);
        actionMap.put(new DeckTransition(DeckState.FULL, DeckState.PULL_CARD), this::pullCard);
        initActions(actionMap);
    }

    public Deck(Deck deck) {
        this();
        cards = deck.cards;
        disposal = deck.disposal;
        card = deck.card;
        actTo(DeckState.FULL);
    }

    @Override
    public String toString() {
        return cards.toString();
    }

    public Card getCard() {
        return card;
    }

    public void actTo(DeckState newState, Collection<Card> cards) {
        this.disposal = cards;
        actTo(newState);
    }

    private void restore() {
        cards.addAll(discarded);
        if (cards.size() != 52) throw new RuleViolationException("Deck is not full");
        discarded.clear();
    }

    private void shuffle() {
        if (cards.size() != 52) throw new RuleViolationException("Deck is not full");
        List<Card> deck = new ArrayList<>(cards);
        Collections.shuffle(deck);
        cards = new LinkedHashSet<>(deck);
    }

    private void pullCard() {
        if (cards.isEmpty()) throw new BadConditionException("Deck is empty");
        this.card = cards.iterator().next();
        cards.remove(this.card);
    }

    private void discard() {
        discarded.addAll(this.disposal);
    }

}
