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
        this.globalResult = getRandomOutput("game.Game");
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

    private String getRandomOutput(String key) {
        String[] messages =  GlobalProperties.outputMessages.get(GlobalProperties.defaultLocale)
                .getString(key).split(";");
        return messages[(int) (Math.random() * (Arrays.stream(messages).count() - 1))];
    }

    public void setBlinds(int smallBlindAmount, int bigBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.globalResult = String.format(getRandomOutput("game.setBlinds"), smallBlindAmount, bigBlindAmount);
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

    public void nextStage(RoundState lastState, Player player) {
        if (table.challengerCount() == 1) {
            System.out.println("We have a winner");
            table.getNextChallengingFrom(player).actTo(PlayerState.OPTIONAL_SHOWDOWN);
            round.actTo(RoundState.OPTIONAL_SHOWDOWN);
        } else if (table.challengerCount() - table.countByState(PlayerState.ALL_IN) == 0) {
            System.out.println("Players should now showdown");
            table.stream().filter(Player::isFolded).forEach(folded -> folded.actTo(PlayerState.SPECTATOR));
            round.getLead().actTo(PlayerState.SHOWDOWN);
            round.actTo(RoundState.SHOWDOWN);
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
            table.stream().filter(Player::isFolded).forEach(folded -> folded.actTo(PlayerState.SPECTATOR));
            round.getLead().actTo(PlayerState.SHOWDOWN);
        } else if (lastState.equals(RoundState.SHOWDOWN)) {
            throw new BadStateException("Unexpected showdown");
        }

    }

    public void doBet(Player player, int betSum) {
        if (round == null) throw new RuleViolationException("Round is not started");
        RoundState lastState = round.getState();
        player.actTo(PlayerState.BETTING, betSum);
        round.actTo(RoundState.TAKE_BET, player);
        player.actTo((player.getStackSum() == betSum) ? PlayerState.ALL_IN : PlayerState.IN_LINE);
        if (Action.stoppers().contains(round.getLastAction()) && (player.equals(round.getLead())
                    || (table.getNextPlayingFrom(player).equals(round.getLead()) && !round.getLead().canAct()))) {
            nextStage(lastState, player);
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
        if (round == null) throw new RuleViolationException("Round is not started");
        RoundState lastState = round.getState();
        player.actTo(PlayerState.FOLDED);
        collectCards(player.getDiscarded());
        if (player.equals(round.getLead())
                || (table.getNextPlayingFrom(player).equals(round.getLead()) && !round.getLead().canAct())
                || (table.activePlayerCount() == 1)) {
            nextStage(lastState, player);
        } else {
            table.getNextActivePlayerFrom(player).actTo(PlayerState.IN_TURN);
        }
    }

    public void doCall(Player player) {
        doBet(player, round.getCallAmount() - round.getPlayerStackSum(player));
    }

    public void doCheck(Player player) {
        doBet(player, 0);
    }

    public void doRaise(Player player, int raiseSum) {
        doBet(player, round.getCallAmount() - round.getPlayerStackSum(player) + raiseSum);
    }

    public void doRaise(Player player) {
        doBet(player, round.getCallAmount() - round.getPlayerStackSum(player) + round.getRaiseAmount());
    }
    public void doAllIn(Player player) {
        doBet(player, player.getStackSum());
    }

    public void doShowFlop(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.FLOP, dealer.getFlop());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
    }

    public void doShowTurn(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.TURN, dealer.getTurn());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
    }

    public void doShowRiver(Player dealer) {
        dealer.actTo(PlayerState.IN_LINE);
        round.actTo(RoundState.RIVER, dealer.getRiver());
        table.getNextPlayingFrom(dealer).actTo(PlayerState.IN_TURN);
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

    public void doDeal() {
        // debug if (table.readyPlayerCount() <= 1) throw new BadStateException("NOT_ENOUGH_PLAYERS");
        table.getById(parsedEvent.getId()).actTo(PlayerState.DEALING, table);
        table.stream().filter(Player::canPlay).forEach(player -> {
                player.actTo(PlayerState.IN_LINE, table.getById(parsedEvent.getId()).getDealtCards().get(player));
                bulkResult.put(player.getId()
                        , table.getById(parsedEvent.getId()).getDealtCards().get(player).toString());
        });
        round = new Round(this);
        round.actTo(RoundState.SMALL_BLIND);
        Player nextPlayer = table.getNextActivePlayerFrom(table.getById(parsedEvent.getId()));
        nextPlayer.actTo(PlayerState.SMALL_BLIND);
        globalResult = String.format(parsedEvent.getOutputString()+ ": " + nextPlayer.getAndClearGlobalMessage()
                , table.getById(parsedEvent.getId()).getName(), nextPlayer.getName(), table.activePlayerCount());
    }

    public void doShuffle() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SHUFFLED);
        globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
    }

    public void doGiveDeck() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.DEALER, deck);
        table.setDealer(table.getById(parsedEvent.getId()));
        globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
    }

    public void doComeBack() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SPECTATOR);
        globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
    }

    public void doGoAway() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.AWAY);
        globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
    }

    public void doCreate() {
        if (!playerExists(parsedEvent.getId())) {
            Player player = new Player(parsedEvent.getId(), parsedEvent.getName(), parsedEvent.getLocale());
            table.addPlayer(player);
            globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
        } else {
            throw new BadStateException(table.getById(parsedEvent.getId()).getState(), table.getById(parsedEvent.getId()).getState());
        }
    }

    public void doJoin() {
        table.getById(parsedEvent.getId()).actTo(PlayerState.SPECTATOR);
        globalResult = String.format(parsedEvent.getOutputString(), parsedEvent.getName());
    }

    public void doByChips() {
        table.getById(parsedEvent.getId()).cashIn(casino, Integer.parseInt(parsedEvent.getArgs().get("sum")));
        globalResult = String.format(parsedEvent.getOutputString() + ": "
                + table.getById(parsedEvent.getId()).getAndClearGlobalMessage(), parsedEvent.getName(), parsedEvent.getArgs().get("sum"));
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
}
