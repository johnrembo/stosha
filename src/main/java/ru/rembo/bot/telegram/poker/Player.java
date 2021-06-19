package ru.rembo.bot.telegram.poker;

import java.util.Collection;

public class Player extends Dealer<PlayerState> {
    private final Stack stack = new Stack();
    private final Hand hand = new Hand();
    private int betSum;
    private Stack lastBet;
    private Casino casino;
    private int cash;
    private final Hand discarded = new Hand();

    public Collection<Card> getDiscarded() {
        return discarded;
    }

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

    public Player(String name) {
        super(name);
        initState(PlayerState.OUT_OF_CHIPS);
        PlayerActionMap actionMap = new PlayerActionMap(this);
        actionMap.put(new PlayerTransition(PlayerState.OUT_OF_CHIPS, PlayerState.LOADED), this::buyChips);
        actionMap.put(new PlayerTransition(PlayerState.LOADED, PlayerState.SPECTATOR), this::joinGame);
        actionMap.put(new PlayerTransition(PlayerState.SPECTATOR, PlayerState.LOADED), this::leaveGame);
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
        actionMap.put(new PlayerTransition(PlayerState.IN_LINE, PlayerState.SHOWDOWN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.ALL_IN, PlayerState.SHOWDOWN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.RANKED), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.DISCARDED), this::discardHand);
        actionMap.put(new PlayerTransition(PlayerState.SHOWDOWN, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.RANKED, PlayerState.COLLECT_CARDS), this::collectCards);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.SHOWDOWN), this::accept);
        actionMap.put(new PlayerTransition(PlayerState.COLLECT_CARDS, PlayerState.RANKED), this::accept);
        initActions(actionMap);
    }

    public Hand getHand() {
        return hand;
    }

    public Stack getLastBet() {
        return lastBet;
    }

    public int getStackSum() {
        return stack.getSum();
    }

    public Hand returnHand() {
        Hand hand = new Hand();
        hand.add(this.hand.getFirst());
        hand.add(this.hand.getSecond());
        this.hand.clear();
        return hand;
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

    public void changeChips(Casino casino, int sum) {
        this.casino = casino;
        this.cash = sum;
        actTo(PlayerState.LOADED);
    }

    public void buyChips() {
        stack.deposit(casino.change(cash));
        System.out.println(name + " buys chips for " + cash);
    }

    public void askChange(int sum, Casino casino) {
        Stack part = stack.withdraw(sum);
        System.out.println(name + " asks to change chips from bank: " + part);
        stack.deposit(casino.change(part));
    }

    public boolean canPlay() {
        return !PlayerState.awayOrOutOfChips().contains(getState());
    }

    public boolean hasChips() {
        return !stack.isEmpty();
    }

    public boolean canAct() {
        return PlayerState.active().contains(getState());
    }

    public boolean isPlaying() {
        return PlayerState.playing().contains(getState());
    }

    public void takeCard(Card card) {
        hand.add(card); // TODO get from Game
    }

    private void discardHand() {
        discarded.addAll(hand);
        System.out.println(name + " discarded hand");
    }

    private void acceptSmallBlind() {
        System.out.println(name + " is on small blind");
    }

    private void acceptBigBlind() {
        System.out.println(name + " is on big blind");
    }

    private void inTurn() {
        System.out.println("It is now " + name + "'s turn");
    }

    private void bet() {
        this.lastBet = stack.getPart(betSum);
        System.out.println(name +  ((betSum == 0) ? " checks" : " bets " + betSum));
    }

    private void fold() {
        System.out.println(name + " is folding");
        discarded.addAll(hand);
    }

    private void takeHand() {
        System.out.println(getName() + " gets cards");
    }

    private void leaveGame() {
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
