package ru.rembo.bot.telegram.poker;

import java.util.*;

public class Round  extends AbstractActor<RoundState> {

    private final Game game;
    private final Stack pot = new Stack();
    private final HashMap<Player, Stack> roundBets = new HashMap<>();
    private final LinkedHashMap<Player, Stack> sidePots = new LinkedHashMap<>();
    private int callAmount = 0;
    private int raiseAmount = 0;
    private final HashSet<Card> sharedCards = new HashSet<>();
    private final HashMap<Player, Combo> ranks = new HashMap<>();
    private Player lead;
    private Player currentPlayer;
    private Action lastAction;
    private Combo combo;

    public static class RoundTransition extends AbstractTransition<RoundState> {
        RoundTransition(RoundState before, RoundState after) {
            super(before, after);
        }
    }

    public static class RoundActionMap extends AbstractActionMap<RoundState> {
        RoundActionMap(ActorBehaviour<RoundState> actorBehaviour) {
            super(actorBehaviour);
        }

        @Override
        public void init(Behaviour<RoundState> behaviour) {
            // static behaviour
        }
    }

    public Round(Game game) {
        this.game = game;
        initState(RoundState.STARTED);
        RoundActionMap actionMap = new RoundActionMap(this);
        actionMap.put(new RoundTransition(RoundState.STARTED, RoundState.SMALL_BLIND), this::accept);
        actionMap.put(new RoundTransition(RoundState.SMALL_BLIND, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.BIG_BLIND), this::nextTurn);
        actionMap.put(new RoundTransition(RoundState.BIG_BLIND, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.PREFLOP), this::nextTurn);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.FLOP), this::nextTurn);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.TURN), this::nextTurn);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.RIVER), this::nextTurn);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.CHOP_THE_POT), this::chopThePot);
        actionMap.put(new RoundTransition(RoundState.PREFLOP, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.FLOP, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.TURN, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.RIVER, RoundState.TAKE_BET), this::takeBet);
        actionMap.put(new RoundTransition(RoundState.PREFLOP, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.FLOP, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.TURN, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.RIVER, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.PREFLOP, RoundState.OPTIONAL_SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.FLOP, RoundState.OPTIONAL_SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.TURN, RoundState.OPTIONAL_SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.RIVER, RoundState.OPTIONAL_SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.WAIT_FLOP), this::nextStage);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.WAIT_TURN), this::nextStage);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.WAIT_RIVER), this::nextStage);
        actionMap.put(new RoundTransition(RoundState.TAKE_BET, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.WAIT_FLOP, RoundState.FLOP), this::showSharedCards);
        actionMap.put(new RoundTransition(RoundState.WAIT_TURN, RoundState.TURN), this::showSharedCards);
        actionMap.put(new RoundTransition(RoundState.WAIT_RIVER, RoundState.RIVER), this::showSharedCards);
        actionMap.put(new RoundTransition(RoundState.SHOWDOWN, RoundState.RANK), this::rank);
        actionMap.put(new RoundTransition(RoundState.OPTIONAL_SHOWDOWN, RoundState.RANK), this::rank);
        actionMap.put(new RoundTransition(RoundState.OPTIONAL_SHOWDOWN, RoundState.HIDDEN_RANK), this::hiddenRank);
        actionMap.put(new RoundTransition(RoundState.RANK, RoundState.SHOWDOWN), this::accept);
        actionMap.put(new RoundTransition(RoundState.SHOWDOWN, RoundState.CHOP_THE_POT), this::chopThePot);
        actionMap.put(new RoundTransition(RoundState.RANK, RoundState.CHOP_THE_POT), this::chopThePot);
        actionMap.put(new RoundTransition(RoundState.HIDDEN_RANK, RoundState.CHOP_THE_POT), this::chopThePot);
        initActions(actionMap);
    }

    public Collection<Card> getSharedCards() {
        return sharedCards;
    }

    public Action getLastAction() {
        return lastAction;
    }

    public Player getLead() {
        return lead;
    }

    public int getCallAmount() {
        return callAmount;
    }

    public int getRaiseAmount() {
        return raiseAmount;
    }

    public void actTo(RoundState newState, Player player) {
        this.currentPlayer = player;
        actTo(newState);
    }

    public void actTo(RoundState newState, LinkedHashSet<Card> cards) {
        this.sharedCards.addAll(cards);
        actTo(newState);
    }

    public void actTo(RoundState newState, Card card) {
        this.sharedCards.add(card);
        actTo(newState);
    }

    private int getPotSum() {
        int sum = 0;
        for (Stack stack : roundBets.values()) {
            sum += stack.getSum();
        }
        for (Stack stack : sidePots.values()) {
            sum += stack.getSum();
        }
        sum += pot.getSum();
        return sum;
    }

    public int getPlayerStackSum(Player player) {
        return (roundBets.containsKey(player)) ? roundBets.get(player).getSum() : 0;
    }

    private Stack withdrawWithChange(Stack pot, int sum) {
        Stack part = new Stack();
        int withdrawable = sum;
        while ((withdrawable > 0) && (part.getSum() < sum)) {
            try {
                part.deposit(pot.withdraw(withdrawable));
            } catch (BadConditionException e) {
                withdrawable -= game.getSmallestChip().getValue();
            }
        }
        if (withdrawable < sum) {
            pot.deposit(game.getCasino().change(pot.withdrawAll()));
            part.deposit(pot.withdraw(sum - withdrawable));
        }
        return part;
    }

    private Stack splitPot(Stack splitPot, HashMap<Player, Combo> winners) {
        String potType = (splitPot.equals(pot)) ? (winners.size() > 1) ? "Split" : "Main"
                : (winners.size() > 1) ? "Split side" : "Side";
        int remain = 0;
        while ((splitPot.getSum() - remain) % winners.size() != 0) {
            remain += game.getSmallestChip().getValue();
        }
        int winSum = (splitPot.getSum() - remain) / winners.size();
        if (winSum > 0) {
            for (Map.Entry<Player, Combo> winner : winners.entrySet()) {
                winner.getKey().takeChips(withdrawWithChange(splitPot, winSum));
                System.out.println(potType + " pot " + winSum + " goes to " + winner.getKey().getName());
            }
        }
        return splitPot;
    }

    private void chopThePot() {
        System.out.println("Round ending");
        roundBets.forEach((player, stack) -> pot.addAll(stack));
        roundBets.clear();
        HashMap<Player, Combo> challengers = new HashMap<>();
        for (Map.Entry<Player, Combo> entry : ranks.entrySet()) {
            if (entry.getValue().rank > 0) challengers.put(entry.getKey(), entry.getValue());
        }
        Stack deadMoney = new Stack();
        while (!challengers.isEmpty()) {
            HashMap<Player, Combo> winners = new HashMap<>();
            int maxRank = 0;
            for (Map.Entry<Player, Combo> entry : challengers.entrySet()) {
                if (entry.getValue().rank > maxRank) maxRank = entry.getValue().rank;
            }
            for (Map.Entry<Player, Combo> rank : challengers.entrySet()) {
                if (rank.getValue().rank == maxRank) {
                    winners.put(rank.getKey(), rank.getValue());
                }
            }

            if (winners.size() > 1) {
                HashSet<Rank> allKickers = new HashSet<>();
                winners.forEach((player, combo) -> combo.kickers.forEach(kicker -> allKickers.add(kicker.rank)));
                List<Rank> kickers = new ArrayList<>(allKickers);
                kickers.sort(new CompareHiAce().reversed());
                for (Rank rank : kickers) {
                    HashSet<Player> kicked = new HashSet<>();
                    winners.forEach(((player, combo) -> {
                        Card found = null;
                        for (Card kicker : combo.kickers) {
                            if (kicker.compareTo(rank) == 0) {
                                found = kicker;
                                break;
                            }
                        }
                        if (found != null) {
                            winners.get(player).setKicker(found);
                            combo.kickers.remove(found);
                        } else {
                            System.out.println(player.getName() + " kicked by " + rank);
                            kicked.add(player);
                        }
                    }));
                    kicked.forEach(winners::remove);
                    if (winners.size() == 0) throw new BadConditionException("Bad kicker calculation");
                    if (winners.size() == 1) break;
                }
            }

            if (winners.isEmpty()) break;

            HashSet<Player> sidePotWinners = new HashSet<>(winners.keySet());
            sidePotWinners.retainAll(sidePots.keySet());
            if (!sidePotWinners.isEmpty()) {
                for (Player winner : winners.keySet()) {
                    Iterator<Player> it = sidePots.keySet().iterator();
                    while (it.hasNext()) {
                        Player player = it.next();
                        deadMoney.addAll(splitPot(sidePots.get(player), winners));
                        it.remove();
                        if ((player == winner) && sidePots.containsKey(winner)) {
                            challengers.remove(winner);
                            break;
                        }
                    }

                }
            } else {
                Iterator<Player> it = sidePots.keySet().iterator();
                while (it.hasNext()) {
                    Player player = it.next();
                    deadMoney.addAll(splitPot(sidePots.get(player), winners));
                    it.remove();
                }
                deadMoney.addAll(splitPot(pot, winners));
                winners.forEach((player, combo) -> challengers.remove(player));
            }
            winners.clear();
        }
        System.out.println("Dead money in pot " + deadMoney);
    }

    private void hiddenRank() {
        HashSet<Card> fullHand = new HashSet<>();
        fullHand.addAll(sharedCards);
        fullHand.addAll(currentPlayer.getOpenHand());
        combo = HandRanking.calcCombo(fullHand);
        ranks.put(currentPlayer, combo);
    }

    private void rank() {
        hiddenRank();
        System.out.println(currentPlayer.getName() + " got " + combo.name + " " + combo.hand + " with high card " + combo.highCard);
        System.out.println(currentPlayer.getName() + " hand is ranked " + combo.rank);
    }

    private void nextTurn() {
        roundBets.get(currentPlayer).deposit(currentPlayer.giveLastBet());
        if (currentPlayer.isEmpty()) {
            sidePots.put(currentPlayer, new Stack());
            System.out.println(currentPlayer.getName() + " is all in!");
        }
        if (Action.continuers().contains(lastAction)) {
            lead = currentPlayer;
        }
    }

    private void takeBet() {
        Stack part = currentPlayer.getLastBet();
        if (!roundBets.containsKey(currentPlayer)) {
            roundBets.put(currentPlayer, new Stack());
        }
        int betSum = roundBets.get(currentPlayer).getSum() + part.getSum();
        if (currentPlayer.getStackSum() != part.getSum() /* TODO add and rules allow low all ins*/) {
            if (betSum < callAmount) throw new RuleViolationException("Cannot take bet less than "
                    + (callAmount - roundBets.get(currentPlayer).getSum()));
            if ((betSum > callAmount) && (betSum - callAmount < raiseAmount) /* TODO add and rules restrict small raise*/)
                throw new RuleViolationException("Cannot raise bet by less than " + raiseAmount);

        }
        if (betSum > callAmount) {
            if (raiseAmount == 0) {
                lastAction = Action.BET;
            } else {
                lastAction = Action.RAISE;
            }
            raiseAmount = betSum - callAmount;
            callAmount = betSum;
            System.out.println("Call amount changed to " + callAmount + ". Minimal raise amount " + raiseAmount);
        } else if (betSum < callAmount) {
            lastAction = Action.ALL_IN;
        } else if (part.getSum() > 0) {
            lastAction = Action.CALL;
        } else if (part.getSum() == 0) {
            lastAction = Action.CHECK;
        } else {
            throw new BadConditionException("Action not resolved");
        }
        System.out.println(currentPlayer.getName() + " last action was " + lastAction);
        System.out.println("Pot size is " + getPotSum());
    }

    private void nextStage() {
        sidePots.forEach((allInPlayer, allInStack) -> {
            if (allInStack.isEmpty()) {
                roundBets.forEach((player, stack) ->
                        allInStack.deposit(withdrawWithChange(stack, roundBets.get(allInPlayer).getSum())));
                allInStack.deposit(pot.withdrawAll());
            }
        });
        roundBets.forEach((player, stack) -> pot.addAll(stack));
        roundBets.clear();
        raiseAmount = 0;
        callAmount = 0;
        lead = currentPlayer;
        if (Action.continuers().contains(lastAction)) {
            lead = currentPlayer;
        }
    }

    private void showSharedCards() {
        System.out.println("Shared cards are " + sharedCards);
    }

}
