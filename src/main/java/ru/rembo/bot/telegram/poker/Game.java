package ru.rembo.bot.telegram.poker;

public class Game {
    private final Table table;
    private final Casino casino;
    private final Deck deck;
    private Round round;
    private int smallBlindAmount = 5, bigBlindAmount = 10;

    public Game(Casino casino, Table table, Deck deck) {
        this.table = table;
        this.casino = casino;
        this.deck = deck;
        System.out.println("Game started. Blinds are "
                + smallBlindAmount + ", " + bigBlindAmount);
    }

    public int getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public int getBigBlindAmount() {
        return bigBlindAmount;
    }

    public void exposeCard(Card card) {
    }

    public void disposeCard(Card card) {

    }

    public Chip getSmallestChip() {
        return casino.getSmallestChip();
    }

    public void shuffleDeck(Player player) {
        player.actTo(PlayerState.SHUFFLED);
    }

    public void join(Player player, Game game) {
        player.actTo(PlayerState.SPECTATOR, game);
        table.addPlayer(player);
    }

    public void giveDeck(Player dealer, Deck deck) {
        dealer.actTo(PlayerState.DEALER, deck);
        table.setDealer(dealer);
    }

    public void doDeal(Player dealer, Table table) {
        dealer.actTo(PlayerState.DEALING, table);
        table.forEach(player -> player.actTo(PlayerState.IN_LINE));
        round = new Round(this);
        round.actTo(RoundState.SMALL_BLIND);
        table.getNextActivePlayerFrom(dealer).actTo(PlayerState.SMALL_BLIND);
    }

    public void doBet(Player player, int betSum) {
        if (round == null) throw new RuleViolationException("Round is not started");
        RoundState lastState = round.getState();
        player.actTo(PlayerState.BETTING, betSum);
        round.actTo(RoundState.TAKE_BET, player);
        player.actTo((player.getStackSum() == betSum) ? PlayerState.ALL_IN : PlayerState.IN_LINE);
        if (Action.stoppers().contains(round.getLastAction()) && (player.equals(round.getLead())
                    || (table.getNextPlayerFrom(player).equals(round.getLead()) && !round.getLead().canAct()))) {
            if (table.activePlayerCount() == 1) {
                round.actTo(RoundState.SHOWDOWN, player);
            } else if (lastState.equals(RoundState.PREFLOP)) {
                round.actTo(RoundState.WAIT_FLOP, table.getDealer());
                System.out.println(table.getDealer().getName() + " should now open Flop");
                table.getDealer().actTo(PlayerState.SHOW_FLOP);
            } else if (lastState.equals(RoundState.FLOP)) {
                round.actTo(RoundState.WAIT_TURN, table.getDealer());
                System.out.println(table.getDealer().getName() + " should now open Turn");
                table.getDealer().actTo(PlayerState.SHOW_TURN);
            } else if (lastState.equals(RoundState.TURN)) {
                round.actTo(RoundState.WAIT_RIVER, table.getDealer());
                System.out.println(table.getDealer().getName() + " should now open River");
                table.getDealer().actTo(PlayerState.SHOW_RIVER);
            } else if (lastState.equals(RoundState.RIVER)) {
                round.actTo(RoundState.SHOWDOWN, player);
                System.out.println("Players now should showdown");
                round.getLead().actTo(PlayerState.SHOWDOWN);
            } else if (lastState.equals(RoundState.SHOWDOWN)) {
                round.actTo(RoundState.CHOP_THE_POT, player);
            }
        } else if (lastState.equals(RoundState.SMALL_BLIND)) {
            round.actTo(RoundState.BIG_BLIND, player);
            table.getNextActivePlayerFrom(player).actTo(PlayerState.BIG_BLIND);
        } else if (lastState.equals(RoundState.BIG_BLIND)) {
            round.actTo(RoundState.PREFLOP, player);
            table.getNextActivePlayerFrom(player).actTo(PlayerState.IN_TURN);
        } else {
            round.actTo(lastState, player);
            table.getNextActivePlayerFrom(player).actTo(PlayerState.IN_TURN);
        }

    }

    public void doFold(Player player) {
        deck.actTo(DeckState.DISCARD, player.returnHand());
        deck.actTo(DeckState.PLAYED);
        player.actTo(PlayerState.FOLDED);
        table.getNextPlayerFrom(player).actTo(PlayerState.IN_TURN);
    }

    public void doCall(Player player) {
        doBet(player, round.getCallAmount() - round.getPlayerStack(player).getSum());
    }

    public void doCheck(Player player) {
        doBet(player, 0);
    }

    public void doRaise(Player player, int raiseSum) {
        doBet(player, round.getCallAmount() - round.getPlayerStack(player).getSum() + raiseSum);
    }

    public void doAllIn(Player player) {
        doBet(player, player.getStackSum());
    }

    public void doShowFlop(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.FLOP, dealer.getFlop());
        table.getNextPlayerFrom(dealer).actTo(PlayerState.IN_TURN);
    }

    public void doShowTurn(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.TURN, dealer.getTurn());
        table.getNextPlayerFrom(dealer).actTo(PlayerState.IN_TURN);
    }

    public void doShowRiver(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.RIVER, dealer.getRiver());
        table.getNextPlayerFrom(dealer).actTo(PlayerState.IN_TURN);
    }

    public void doRank(Player player) {
        player.actTo(PlayerState.RANKED);
        round.actTo(RoundState.RANK, player);
        round.actTo((table.allPlayersAreOpen()) ? RoundState.CHOP_THE_POT : RoundState.SHOWDOWN);
        table.getNextActivePlayerFrom(player).actTo(PlayerState.SHOWDOWN);
    }

    public void doDiscard(Player player) {
        player.actTo(PlayerState.DISCARDED);
        PlayerState lastState = table.getDealer().getState();
        table.getDealer().actTo(PlayerState.COLLECT_CARDS, player.getDiscarded());
        table.getDealer().actTo(lastState);
        if (table.allPlayersAreOpen()) {
            round.actTo(RoundState.CHOP_THE_POT);
        }
    }

    public Casino getCasino() {
        return casino;
    }

}
