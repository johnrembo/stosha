package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.statemachine.AbstractActionMap;
import ru.rembo.bot.telegram.statemachine.AbstractTransition;
import ru.rembo.bot.telegram.statemachine.ActorBehaviour;
import ru.rembo.bot.telegram.statemachine.Behaviour;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class Player extends Dealer<PlayerState> {
    private final Stack stack = new Stack();
    private final Hand hand = new Hand();
    private int betSum;
    private Stack lastBet;
    private Casino casino;
    private int cash;
    private final Collection<Card> discarded = new HashSet<>();
    private int wallet = 1000;
    private final int id;
    private String globalMessage = "";
    private String privateMessage = "";
    private final Locale locale;

    public static class PlayerTransition extends AbstractTransition<PlayerState> {
        PlayerTransition(PlayerState before, PlayerState after) {
            super(before, after);
        }
    }

    public static class PlayerActionMap extends AbstractActionMap<PlayerState> {
        PlayerActionMap(ActorBehaviour<PlayerState> actorBehaviour) {
            super(actorBehaviour);
        }

        @Override
        public void init(Behaviour<PlayerState> behaviour) {
            // static behaviour
        }
    }

    public Player(int id, String name, Locale locale) {
        super(name);
        this.id = id;
        this.locale = locale;
        initState(PlayerState.OUT_OF_CHIPS);
        PlayerActionMap actionMap = new PlayerActionMap(this);
        actionMap.put(new PlayerTransition(PlayerState.OUT_OF_CHIPS, PlayerState.LOADED), this::buyChips);
        actionMap.put(new PlayerTransition(PlayerState.LOADED, PlayerState.LOADED), this::buyChips);
        actionMap.put(new PlayerTransition(PlayerState.LOADED, PlayerState.SPECTATOR), this::joinGame);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.AWAY), this::goAway);
        actionMap.put(new PlayerTransition(PlayerState.AWAY, PlayerState.SPECTATOR), this::comeBack);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.DEALER), this::takeDeck);
        actionMap.put(new PlayerTransition(PlayerState.DEALER, PlayerState.SHUFFLED), this::shuffleDeck);
        actionMap.put(new PlayerTransition(PlayerState.SHUFFLED, PlayerState.DEALING), this::deal);
        actionMap.put(new PlayerTransition(PlayerState.DEALING, PlayerState.IN_LINE), this::takeHand);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.IN_LINE), this::takeHand);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SMALL_BLIND), this::acceptSmallBlind);
        actionMap.put(new PlayerTransition(PlayerState.SMALL_BLIND, PlayerState.BETTING), this::bet);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.BIG_BLIND), this::acceptBigBlind);
        actionMap.put(new PlayerTransition(PlayerState.BIG_BLIND, PlayerState.BETTING), this::bet);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.IN_TURN), this::inTurn);
        actionMap.put(new PlayerTransition(PlayerState.IN_TURN, PlayerState.BETTING), this::bet);
        actionMap.put(new PlayerTransition(PlayerState.BETTING, PlayerState.IN_LINE), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.BETTING, PlayerState.ALL_IN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.IN_TURN, PlayerState.FOLDED), this::fold);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SHOW_FLOP), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SHOW_TURN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SHOW_RIVER), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.SHOW_FLOP), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.SHOW_TURN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.SHOW_RIVER), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.SHOW_FLOP, PlayerState.IN_LINE), this::showFlop);
        actionMap.put(new PlayerTransition(PlayerState.SHOW_TURN, PlayerState.IN_LINE), this::showTurn);
        actionMap.put(new PlayerTransition(PlayerState.SHOW_RIVER, PlayerState.IN_LINE), this::showRiver);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SHOWDOWN), this::showDown);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.OPTIONAL_SHOWDOWN), this::showDownOptional);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.SHOWDOWN), this::showDown);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.RANKED), this::openHand);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.RANKED), this::openHand);
        actionMap.put(new PlayerTransition(PlayerState.OPTIONAL_SHOWDOWN, PlayerState.RANKED_HIDDEN), this::hideHand);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.SPECTATOR), this::discardHand);
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.IN_TURN, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.FOLDED, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.RANKED, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.RANKED_HIDDEN, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.IN_LINE), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.IN_TURN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.ALL_IN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.SHOWDOWN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.FOLDED), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.RANKED), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.RANKED_HIDDEN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.SPECTATOR), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.FOLDED, PlayerState.SPECTATOR), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.RANKED, PlayerState.SPECTATOR), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.RANKED_HIDDEN, PlayerState.SPECTATOR), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.RETURN_DECK), this::returnDeck);
        actionMap.put(new PlayerTransition(PlayerState.RETURN_DECK, PlayerState.SPECTATOR), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.DEALER, PlayerState.SHOW_FROM_TOP), this::showCardFromTop);
        actionMap.put(new PlayerTransition(PlayerState.SHUFFLED, PlayerState.SHOW_FROM_TOP), this::showCardFromTop);
        actionMap.put(new PlayerTransition(PlayerState.SHOW_FROM_TOP, PlayerState.SHUFFLED), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.OUT_OF_CHIPS), this::sellChips);
        actionMap.put(new PlayerTransition(PlayerState.LOADED, PlayerState.OUT_OF_CHIPS), this::sellChips);
        actionMap.put(new PlayerTransition(PlayerState.AWAY, PlayerState.OUT_OF_CHIPS), this::sellChips);
        actionMap.put(new PlayerTransition(PlayerState.OUT_OF_CHIPS, PlayerState.OUT_OF_CHIPS), this::accept);
        initActions(actionMap);
    }

    public int getId() {
        return id;
    }

    public String getAndClearGlobalMessage() {
        String string = globalMessage;
        globalMessage = "";
        return string;
    }

    public String getAndClearPrivateMessage() {
        String string = privateMessage;
        privateMessage = "";
        return string;
    }

    public Stack getLastBet() {
        return lastBet;
    }

    public int getStackSum() {
        return stack.getSum();
    }

    public void actTo(PlayerState newState, int betSum) {
        this.betSum = betSum;
        actTo(newState);
    }

    public void takeChips(Stack chips) {
        stack.deposit(chips);
    }

    public Stack giveLastBet() {
        return stack.withdraw(lastBet);
    }

    public void cashIn(Casino casino, int sum) {
        this.casino = casino;
        this.cash = sum;
        actTo(PlayerState.LOADED);
    }

    public void cashOut(Casino casino) {
        this.casino = casino;
        actTo(PlayerState.OUT_OF_CHIPS);
    }

    public Collection<Card> getDiscarded() {
        return discarded;
    }

    public Collection<Card>  getOpenHand() {
        return discarded;
    }

    private void buyChips() {
        System.out.println(name + " buys chips for " + cash);
        Stack chips = new Stack(casino.change(cash));
        stack.deposit(chips);
        globalMessage = chips.toString();
        privateMessage = stack.toString();
        this.wallet = this.wallet - cash;
    }

    private void sellChips() {
        System.out.println(name + " sells chips for " + stack.getSum());
        this.wallet = this.wallet +  casino.cashOut(stack.withdrawAll());
    }

    public void exchange(int sum, Casino casino) {
        Stack part = stack.withdraw(sum);
        System.out.println(name + " asks to change chips from bank: " + part);
        stack.deposit(casino.change(part));
    }

    public boolean canPlay() {
        return !PlayerState.awayOrOutOfChips().contains(getState());
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean canAct() {
        return PlayerState.active().contains(getState());
    }

    public boolean isPlaying() {
        return PlayerState.playing().contains(getState());
    }

    public boolean isFolded() {
        return getState().equals(PlayerState.FOLDED);
    }

    public boolean isSpectating() {
        return getState().equals(PlayerState.SPECTATOR);
    }

    public boolean inChallenge() {
        return PlayerState.challenging().contains(getState());
    }

    private void acceptSmallBlind() {
        globalMessage = GlobalProperties.getRandomOutput("player.acceptSmallBlind", locale);
        System.out.println(name + " is on small blind");
    }

    private void acceptBigBlind() {
        globalMessage = GlobalProperties.getRandomOutput("player.acceptBigBlind", locale);
        System.out.println(name + " is on big blind");
    }

    private void inTurn() {
        System.out.println("It is now " + name + "'s turn");
    }

    private void bet() {
        this.lastBet = stack.getPart(betSum);
        System.out.println(name +  ((betSum == 0) ? " checks" : " bets " + betSum));
    }

    private void showDownOptional() {
        System.out.println(name + " can show or hide hand");
    }

    private void showDown() {
        System.out.println(name + " should showdown or discard");
    }

    private void hideHand() {
        System.out.println(name + " hides hand");
        discarded.clear();
        discarded.addAll(hand);
        hand.clear();
    }

    private void openHand() {
        System.out.println(name + " opens hand " + hand);
        discarded.clear();
        discarded.addAll(hand);
        hand.clear();
    }

    private void discardHand() {
        System.out.println(name + " discards hand");
        discarded.clear();
        discarded.addAll(hand);
        hand.clear();
    }

    private void fold() {
        System.out.println(name + " folds");
        discarded.clear();
        discarded.addAll(hand);
        hand.clear();
    }

    private void takeHand() {
        hand.addAll(cards);
        System.out.println(getName() + " gets cards");
    }

    private void leaveGame() {
        globalMessage = GlobalProperties.getRandomOutput("player.leaveGame", locale);
        System.out.println(getName() + " leaves game");
    }

    private void joinGame() {
        System.out.println(getName() + " joins game");
    }

    private void goAway() {
        System.out.println(getName() + " goes away");
    }

    private void comeBack() {
        System.out.println(getName() + " comes back");
    }

    // TODO pass chips to other players
}
