package ru.rembo.bot.telegram.poker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class Dealer<T extends Enum<T>> extends AbstractActor<T> {
    protected String name;
    protected Deck deck;
    protected Game game;
    private Table table;
    private final LinkedHashSet<Card> flop = new LinkedHashSet<>();
    private Card turn;
    private Card river;
    protected Collection<Card> cards;
    private final HashMap<Player, Hand> dealtCards = new HashMap<>();
    private Deck playedDeck;

    public Dealer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public HashMap<Player, Hand> getDealtCards() {
        return dealtCards;
    }

    public void actTo(T state, Collection<Card> cards) {
        this.cards = cards;
        actTo(state);
    }
    public void actTo(T state, Deck deck) {
        this.deck = deck;
        actTo(state);
    }

    public void actTo(T state, Game game) {
        this.game = game;
        actTo(state);
    }

    public void actTo(T state, Table table) {
        this.table = table;
        actTo(state);
    }

    public Collection<Card> getCards() {
        return cards;
    }

    public Card getTurn() {
        return turn;
    }

    public Card getRiver() {
        return river;
    }

    public Deck getPlayedDeck() {
        return playedDeck;
    }
    protected void takeDeck() {
        System.out.println(getName() + " takes deck (" + deck.size() + "): " + deck);
    }

    protected void shuffleDeck() {
        System.out.println(getName() + " shuffles the deck");
        deck.actTo(DeckState.SHUFFLED);
    }

    public LinkedHashSet<Card> getFlop() {
        return flop;
    }

    protected void showRiver() {
        System.out.println(name + " showing River");
        Collection<Card> burn = new HashSet<>();
        deck.actTo(DeckState.PULL_CARD);
        burn.add(deck.getCard());
        deck.actTo(DeckState.DISCARD, burn);
        deck.actTo(DeckState.PULL_CARD);
        river = deck.getCard();
        deck.actTo(DeckState.PLAYED);
    }

    protected void showTurn() {
        System.out.println(name + " showing Turn");
        Collection<Card> burn = new HashSet<>();
        deck.actTo(DeckState.PULL_CARD);
        burn.add(deck.getCard());
        deck.actTo(DeckState.DISCARD, burn);
        deck.actTo(DeckState.PULL_CARD);
        turn = deck.getCard();
        deck.actTo(DeckState.PLAYED);
    }

    protected void showFlop() {
        System.out.println(name + " showing Flop");
        Collection<Card> burn = new HashSet<>();
        deck.actTo(DeckState.PULL_CARD);
        burn.add(deck.getCard());
        deck.actTo(DeckState.DISCARD, burn);
        deck.actTo(DeckState.PULL_CARD);
        flop.add(deck.getCard());
        deck.actTo(DeckState.PULL_CARD);
        flop.add(deck.getCard());
        deck.actTo(DeckState.PULL_CARD);
        flop.add(deck.getCard());
        deck.actTo(DeckState.PLAYED);
    }

    protected void deal() {
        System.out.println(getName() + " deals cards");
        table.stream().filter(Player::canPlay).forEach(player -> {
            deck.actTo(DeckState.PULL_CARD);
            dealtCards.put(player, new Hand());
            dealtCards.get(player).add(deck.getCard());
            deck.actTo(DeckState.PLAYED);
        });
        table.stream().filter(Player::canPlay).forEach(player -> {
            deck.actTo(DeckState.PULL_CARD);
            dealtCards.get(player).add(deck.getCard());
            deck.actTo(DeckState.PLAYED);
        });
    }

    protected void collectCards() {
        deck.actTo(DeckState.DISCARD, getCards());
    }

    protected void returnDeck() {
        deck.actTo(DeckState.FULL);
        playedDeck = new Deck(deck);
        deck = null;
    }

    protected void showCardFromTop() {
        Collection<Card> burn = new HashSet<>();
        deck.actTo(DeckState.PULL_CARD);
        System.out.println(name + " showing card from top " + deck.getCard());
        burn.add(deck.getCard());
        deck.actTo(DeckState.DISCARD, burn);
        deck.actTo(DeckState.FULL);
    }
}
