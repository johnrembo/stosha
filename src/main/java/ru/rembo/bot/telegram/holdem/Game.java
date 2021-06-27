package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.statemachine.BadStateException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class Game {
    private final Deck deck = new Deck();
    private final Table table = new Table();
    private final Casino casino = new Casino(5);
    private Round round;
    private int smallBlindAmount = 5, bigBlindAmount = 10;
    private String globalResult;
    private final HashMap<Integer, String> bulkResult = new HashMap<>();
    private HoldemParsedEvent parsedEvent;

    public Game(long ID, String name) {
        this.globalResult = GlobalProperties.getRandomOutput("game.Game", GlobalProperties.defaultLocale);
    }

    public Table getTable() {
        return table;
    }

    public int getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public int getBigBlindAmount() {
        return bigBlindAmount;
    }

    public void setBlinds(int smallBlindAmount, int bigBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.globalResult = String.format(GlobalProperties.getRandomOutput("game.setBlinds"
                , GlobalProperties.defaultLocale), smallBlindAmount, bigBlindAmount);
    }

    public Casino getCasino() {
        return casino;
    }

    public Chip getSmallestChip() {
        return casino.getSmallestChip();
    }

    public void giveDeck(Player dealer, Deck deck) {
        dealer.actTo(PlayerState.DEALER, deck);
        table.setDealer(dealer);
    }

    public void nextStage(RoundState lastState, Player player, StringBuilder builder) {
        if (table.challengerCount() == 1) {
            System.out.println("We have a winner");
            table.getNextChallengingFrom(player).actTo(PlayerState.OPTIONAL_SHOWDOWN);
            round.actTo(RoundState.OPTIONAL_SHOWDOWN);
            builder.append("\n").append(GlobalProperties.getRandomOutput("game.winner", parsedEvent.getLocale()));
            builder.append("\n").append(player.getAndClearGlobalMessage(player.getName()));
        } else if ((table.challengerCount() - table.countByState(PlayerState.ALL_IN) == 0)
                    || lastState.equals(RoundState.RIVER)) {
            System.out.println("Players should now showdown");
            table.stream().filter(Player::isFolded).forEach(folded -> folded.actTo(PlayerState.SPECTATOR));
            round.getLead().actTo(PlayerState.SHOWDOWN);
            round.actTo(RoundState.SHOWDOWN);
            builder.append("\n").append(round.getLead().getAndClearGlobalMessage(round.getLead().getName()));
        } else if (lastState.equals(RoundState.PREFLOP)) {
            round.actTo(RoundState.WAIT_FLOP, table.getDealer());
            System.out.println(table.getDealer().getName() + " should now open Flop");
            table.getDealer().actTo(PlayerState.SHOW_FLOP);
            builder.append("\n").append(table.getDealer().getAndClearGlobalMessage(table.getDealer().getName()));
        } else if (lastState.equals(RoundState.FLOP)) {
            round.actTo(RoundState.WAIT_TURN, table.getDealer());
            System.out.println(table.getDealer().getName() + " should now open Turn");
            table.getDealer().actTo(PlayerState.SHOW_TURN);
            builder.append("\n").append(table.getDealer().getAndClearGlobalMessage(table.getDealer().getName()));
        } else if (lastState.equals(RoundState.TURN)) {
            round.actTo(RoundState.WAIT_RIVER, table.getDealer());
            System.out.println(table.getDealer().getName() + " should now open River");
            table.getDealer().actTo(PlayerState.SHOW_RIVER);
            builder.append("\n").append(table.getDealer().getAndClearGlobalMessage(table.getDealer().getName()));
        } else if (lastState.equals(RoundState.SHOWDOWN)) {
            throw new BadStateException("Unexpected showdown");
        }

    }

    public void doRank(Player player) {
        player.actTo(PlayerState.RANKED);
        round.actTo(RoundState.RANK, player);
        collectCards(player.getOpenHand());
        player.actTo(PlayerState.SPECTATOR);
        if (table.getNextActivePlayerFrom(player).getState().equals(PlayerState.SPECTATOR)) {
            endRound();
        } else {
            round.actTo(RoundState.SHOWDOWN);
            table.getNextActivePlayerFrom(player).actTo(PlayerState.SHOWDOWN);
        }
    }

    public void doHiddenRank(Player player) {
        player.actTo(PlayerState.RANKED_HIDDEN);
        round.actTo(RoundState.HIDDEN_RANK, player);
        collectCards(player.getOpenHand());
        player.actTo(PlayerState.SPECTATOR);
        endRound();
    }


    public void doDiscard(Player player) {
        player.actTo(PlayerState.SPECTATOR);
        collectCards(player.getDiscarded());
        if (table.getNextActivePlayerFrom(player).getState().equals(PlayerState.SPECTATOR)) {
            endRound();
        } else {
            table.getNextActivePlayerFrom(player).actTo(PlayerState.SHOWDOWN);
        }
    }

    private void collectCards(Collection<Card> cards) {
        PlayerState lastState = table.getDealer().getState();
        table.getDealer().actTo(PlayerState.COLLECT_CARDS, cards);
        table.getDealer().actTo(lastState);
    }

    private void endRound() {
        collectCards(round.getSharedCards());
        round.actTo(RoundState.CHOP_THE_POT);
        table.getDealer().actTo(PlayerState.RETURN_DECK);
        table.getDealer().actTo(PlayerState.SPECTATOR);
        table.stream().filter(Player::isEmpty).forEach(player -> player.actTo(PlayerState.OUT_OF_CHIPS));
        giveDeck(table.getNextPlayerFrom(table.getDealer(), Player::isSpectating), table.getDealer().getPlayedDeck());
    }

    public void doShowCard(Player dealer) {
        dealer.actTo(PlayerState.SHOW_FROM_TOP);
        dealer.actTo(PlayerState.SHUFFLED);
    }

    public void doShowFlop() {
        Player dealer = table.getById(parsedEvent.getId());
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.FLOP, dealer.getFlop());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
        globalResult = parsedEvent.getOutputString(round.getSharedCards());
    }

    public void doShowTurn() {
        Player dealer = table.getById(parsedEvent.getId());
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.TURN, dealer.getTurn());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
        globalResult = parsedEvent.getOutputString(round.getSharedCards());
    }

    public void doShowRiver() {
        Player dealer = table.getById(parsedEvent.getId());
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.RIVER, dealer.getRiver());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
        globalResult = parsedEvent.getOutputString(round.getSharedCards());
    }

    public void doFold() {
        if (round == null) throw new RuleViolationException("Round is not started");
        RoundState lastState = round.getState();
        Player player = table.getById(parsedEvent.getId());
        StringBuilder builder = new StringBuilder();
        player.actTo(PlayerState.FOLDED);
        builder.append("\n").append(player.getAndClearGlobalMessage(player.getName()));
        collectCards(player.getDiscarded());
        if (player.equals(round.getLead())
                || (table.getNextPlayingFrom(player).equals(round.getLead()) && !round.getLead().canAct())
                || (table.activePlayerCount() == 1)) {
            nextStage(lastState, player, builder);
        } else {
            Player nextPlayer = table.getNextActivePlayerFrom(player);
            nextPlayer.actTo(PlayerState.IN_TURN);
            builder.append("\n").append(nextPlayer.getAndClearGlobalMessage(nextPlayer.getName()));
        }
    }

    public void doCall() {
        bet(table.getById(parsedEvent.getId()), round.getCallAmount() - round.getPlayerStackSum(table.getById(parsedEvent.getId())));
    }

    public void doCheck() {
        bet(table.getById(parsedEvent.getId()), 0);
    }

    public void doRaise() {
        bet(table.getById(parsedEvent.getId()), round.getCallAmount() - round.getPlayerStackSum(table.getById(parsedEvent.getId())) + round.getRaiseAmount());
    }
    public void doAllIn() {
        bet(table.getById(parsedEvent.getId()), table.getById(parsedEvent.getId()).getStackSum());
    }

    public void doBet() {
        bet(table.getById(parsedEvent.getId()), Integer.parseInt(parsedEvent.getArgs().get("sum")));
    }

    private void bet(Player player, int betSum) {
        RoundState lastState = round.getState();
        player.actTo(PlayerState.BETTING, betSum);
        round.actTo(RoundState.TAKE_BET, player);
        StringBuilder builder = new StringBuilder();
        builder.append(parsedEvent.getOutputString(player.getName(), betSum, round.getCallAmount()
                , round.getRaiseAmount(), round.getLastAction()))
                .append("\n").append(player.getAndClearGlobalMessage());
        player.actTo((player.getStackSum() == betSum) ? PlayerState.ALL_IN : PlayerState.IN_LINE);
        Player nextPlayer = table.getNextPlayingFrom(player);
        if (Action.stoppers().contains(round.getLastAction()) && (player.equals(round.getLead())
                || (table.getNextPlayingFrom(player).equals(round.getLead()) && !round.getLead().canAct()))) {
            nextStage(lastState, player, builder);
        } else if (lastState.equals(RoundState.SMALL_BLIND)) {
            round.actTo(RoundState.BIG_BLIND, player);
            nextPlayer.actTo(PlayerState.BIG_BLIND);
            builder.append("\n").append(nextPlayer.getAndClearGlobalMessage(nextPlayer.getName()));
        } else if (lastState.equals(RoundState.BIG_BLIND)) {
            round.actTo(RoundState.PREFLOP, player);
            nextPlayer.actTo(PlayerState.IN_TURN);
            builder.append("\n").append(nextPlayer.getAndClearGlobalMessage(nextPlayer.getName()));
        } else {
            round.actTo(lastState, player);
            nextPlayer.actTo(PlayerState.IN_TURN);
            builder.append("\n").append(nextPlayer.getAndClearGlobalMessage(nextPlayer.getName()));
        }
        globalResult = builder.toString();
    }

    public void doDeal() {
        // debug if (table.readyPlayerCount() <= 1) throw new BadStateException("NOT_ENOUGH_PLAYERS");
        Player dealer = table.getById(parsedEvent.getId());
        dealer.actTo(PlayerState.DEALING, table);
        table.stream().filter(Player::canPlay).forEach(player -> {
                player.actTo(PlayerState.IN_LINE, dealer.getDealtCards().get(player));
                bulkResult.put(player.getId(), dealer.getDealtCards().get(player).toString());
        });
        round = new Round(this);
        round.actTo(RoundState.SMALL_BLIND);
        Player nextPlayer = table.getNextActivePlayerFrom(dealer);
        nextPlayer.actTo(PlayerState.SMALL_BLIND);
        globalResult = parsedEvent.getOutputString(dealer.getName(), table.activePlayerCount())
                + "\n" + nextPlayer.getAndClearGlobalMessage(nextPlayer.getName());
    }

    public void doShuffle() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SHUFFLED);
        globalResult = parsedEvent.getOutputString(parsedEvent.getName());
    }

    public void doGiveDeck() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.DEALER, deck);
        table.setDealer(table.getById(parsedEvent.getId()));
        globalResult = parsedEvent.getOutputString(parsedEvent.getName());
    }

    public void doComeBack() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SPECTATOR);
        globalResult = parsedEvent.getOutputString(parsedEvent.getName());
    }

    public void doGoAway() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.AWAY);
        globalResult = parsedEvent.getOutputString(parsedEvent.getName());
    }

    public void doCreate() {
        if (!playerExists(parsedEvent.getId())) {
            Player player = new Player(parsedEvent.getId(), parsedEvent.getName(), parsedEvent.getLocale());
            table.addPlayer(player);
            globalResult = parsedEvent.getOutputString(parsedEvent.getName());
        } else {
            throw new BadStateException(table.getById(parsedEvent.getId()).getState()
                    , table.getById(parsedEvent.getId()).getState());
        }
    }

    public void doJoin() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SPECTATOR);
        globalResult = parsedEvent.getOutputString(parsedEvent.getName());
    }

    public void doByChips() {
        Player player = table.getById(parsedEvent.getId());
        player.cashIn(casino, Integer.parseInt(parsedEvent.getArgs().get("sum")));
        globalResult = parsedEvent.getOutputString(player.getName()
                , Integer.parseInt(parsedEvent.getArgs().get("sum")))
                + ":" + player.getAndClearGlobalMessage(player.getName());
        bulkResult.put(parsedEvent.getId(), table.getById(parsedEvent.getId()).getAndClearPrivateMessage());
    }

    public String getGlobalResult() {
        return globalResult;
    }

    public boolean playerExists(int id) {
        return table.containsPlayer(id);
    }

    public void setParsedEvent(HoldemParsedEvent parsedEvent) {
        this.parsedEvent = parsedEvent;
    }

    public void clearGlobalResult() {
        globalResult = "";
    }

    public HashMap<Integer, String> getBulkResult() {
        return bulkResult;
    }

    public void clearBulkResult() {
        bulkResult.clear();
    }

    public boolean roundNotStarted() {
        return round == null;
    }
}
