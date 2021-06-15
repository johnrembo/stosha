package ru.rembo.bot.telegram.poker;

import java.util.*;

public class Round {

    private final Game game;
    private boolean roundIsOver = false;
    private final int smallBlind, bigBlind;
    private boolean smallBlindMade = false, bigBlindMade = false;
    private final Chip smallestChip;
    private final Stack pot = new Stack();
    private final HashMap<Player, Stack> roundBets = new HashMap<>();
    private final LinkedHashMap<Player, Stack> sidePots = new LinkedHashMap<>();
    private int callAmount;
    private int raiseAmount;
    private Player raiser;
    private final Table table;
    private final HashSet<Player> foldedPlayers = new HashSet<>();
    private RoundStage stage = RoundStage.PREFLOP;
    private RoundStage requiredStage = RoundStage.NONE;
    private final HashSet<Card> sharedCards = new HashSet<>();
    private final HashSet<Card> burnedCards = new HashSet<>();
    private final HashMap<Player, Combo> ranks = new HashMap<>();

    public Round(Game game, Dealer dealer) {
        this.game = game;
        smallBlind = game.getSmallBlindAmount();
        bigBlind = game.getBigBlindAmount();
        smallestChip = game.getSmallestChip();
        table = game.getTable();
        if (table.activePlayerCount() < 2)
            throw new RuleViolationException("Can't start round with "
                    + table.activePlayerCount() + " player(s)");
        this.callAmount = 0;
        this.raiseAmount = 0;
        table.forEach(player -> { if (player.hasChips() && player.isAway()) roundBets.put(player, new Stack()); });
        System.out.println("Round started");
        table.setDealer(dealer);
    }

    public int getCallAmount() {
        if (isOver()) throw new BadConditionException("Round is over");
        return callAmount;
    }

    public int getRaiseAmount() {
        if (isOver()) throw new BadConditionException("Round is over");
        return raiseAmount;
    }

    public boolean isOver() {
        return roundIsOver;
    }

    public Action takeBet(Player betMaker, Stack part) {
        Action result;
        int betSum = roundBets.get(betMaker).getSum() + part.getSum();
        if (!smallBlindMade) {
            if (!betMaker.equals(table.getSmallBlind()))
                throw new RuleViolationException(betMaker.getName() + " is not at small blind");
            if (part.getSum() != smallBlind) throw new RuleViolationException("Small blind is " + smallBlind);
            smallBlindMade = true;
            System.out.println("Small blind made");
        } else if (!bigBlindMade) {
            if (!betMaker.equals(table.getBigBlind()))
                throw new RuleViolationException(betMaker.getName() + " is not at big blind");
            if (part.getSum() != bigBlind) throw new RuleViolationException("Big blind is " + bigBlind);
            bigBlindMade = true;
            System.out.println("Big blind made");
        }
        if (!sidePots.containsKey(betMaker)) {
            if (betSum < callAmount) throw new RuleViolationException("Cannot take bet less than "
                        + (callAmount - roundBets.get(betMaker).getSum()));
            if (betSum > callAmount) {
                if (betSum - callAmount < raiseAmount)
                    throw new RuleViolationException("Cannot raise bet by less than " + raiseAmount);
                if ((betSum - callAmount) % smallBlind != 0)
                    throw new RuleViolationException("Raise amount must be divisible by " + smallBlind);
            }
        }
        if (betSum > callAmount) {
            if (raiseAmount == 0) {
                result = Action.BET;
            } else {
                result = Action.RAISE;
            }
            raiseAmount = betSum - callAmount;
            callAmount = betSum;
        } else if ((betSum == callAmount) && (part.getSum() > 0)) {
            result = Action.CALL;
        } else if ((betSum == callAmount) && (part.getSum() == 0)) {
            result = Action.CHECK;
        } else {
            throw new BadConditionException("Action not resolved");
        }
        roundBets.get(betMaker).addAll(betMaker.give(part));
        System.out.println(betMaker.getName() + " action " + result);
        if (!betMaker.hasChips()) {
            sidePots.put(betMaker, new Stack());
            System.out.println(betMaker.getName() + " is all in!");
        }
        System.out.println("Pot amount is " + getPotSum());
        return result;
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

    public void stake(Player betMaker, Stack chips) {
        if (isOver()) throw new BadConditionException("Round is over");
        if (!betMaker.hasCards()) throw new RuleViolationException(betMaker.getName() + " does not have cards");
        if (requiredStage != RoundStage.NONE) throw new RuleViolationException(requiredStage + " is required");
        if (betMaker.equals(table.currentPlayer())) {
            Action action = takeBet(betMaker, chips);
            if (Action.stoppers().contains(action)) {
                if (betMaker.equals(raiser)
                        || (table.getNextPlayerFrom(betMaker).equals(raiser) && !raiser.isActive())) {
                    System.out.println("All bets are made");
                    sidePots.forEach((allInPlayer, allInStack) -> {
                        if (allInStack.isEmpty()) {
                            roundBets.forEach((player, stack) ->
                                    allInStack.deposit(stack.withdrawAll(roundBets.get(allInPlayer).getSum())));
                            allInStack.deposit(pot.withdrawAll(pot.getSum()));
                        }
                    });
                    roundBets.forEach((player, stack) -> pot.addAll(stack));
                    roundBets.forEach((player, stack) -> stack.clear());
                    raiseAmount = 0;
                    callAmount = 0;
                    raiser = table.getDealer();
                    requireNextStage();
                }
            } else if (Action.continuers().contains(action) && !chips.isEmpty()) {
                raiser = betMaker;
            }
        } else {
            throw new RuleViolationException("It is not " + betMaker.getName() + "'s call");
        }
        if (requiredStage.equals(RoundStage.NONE)) {
            switchToNextVoterFrom(table.currentPlayer());
        }
    }

    private void requireNextStage() {
        switch (stage) {
            case PREFLOP: requiredStage = RoundStage.FLOP; break;
            case FLOP: requiredStage = RoundStage.TURN; break;
            case TURN: requiredStage = RoundStage.RIVER; break;
            case RIVER:
                requiredStage = RoundStage.SHOWDOWN;
                stage = requiredStage;
                System.out.println("Players should now showdown");
                table.setCurrentPlayer(raiser);
                System.out.println(table.currentPlayer().getName() + " is first to open hand");
                break;
            default:
                throw new BadConditionException("Wrong round stage " + stage);
        }
        if (!requiredStage.equals(RoundStage.SHOWDOWN)) {
            System.out.println("Dealer should now show " + requiredStage);
        }
    }

    public Stack getPlayerStack(Player player) {
        if (isOver()) throw new BadConditionException("Round is over");
        return roundBets.get(player);
    }

    public void fold(Player player) {
        if (isOver()) throw new BadConditionException("Round is over");
        if (!player.equals(table.currentPlayer()))
            throw new RuleViolationException("It is not " + player.getName() + "'s call");
        foldedPlayers.add(player);
        switchToNextVoterFrom(table.currentPlayer());
    }

    public void switchToNextVoterFrom(Player player) {
        table.setCurrentPlayer(table.getNextActivePlayerFrom(player));
        while (foldedPlayers.contains(table.currentPlayer())) {
            if (table.currentPlayer().equals(player)) {
                throw new BadConditionException("No active players left");
            }
            table.switchToNextActivePlayer();
        }
        if (table.currentPlayer().equals(player)
                || (roundBets.size() - foldedPlayers.size() - sidePots.size() == 1)) {
            requireNextStage();
        } else {
            System.out.println("It is now " + table.currentPlayer().getName() + "'s call");
        }
    }

    public void turnOver(Card card) {
        if (requiredStage.equals(RoundStage.NONE))
            throw new RuleViolationException("Turing cards over is not required");
        sharedCards.add(card);
        if (requiredStage.equals(RoundStage.FLOP) && (sharedCards.size() == 3)) {
            stage = requiredStage;
            requiredStage = RoundStage.NONE;
            System.out.println("Flop is open. Shared cards are " + sharedCards);
        } else if (requiredStage.equals(RoundStage.TURN) && (sharedCards.size() == 4)) {
            stage = requiredStage;
            requiredStage = RoundStage.NONE;
            System.out.println("Turn is open. Shared cards are " + sharedCards );
        } else if (requiredStage.equals(RoundStage.RIVER) && (sharedCards.size() == 5)) {
            stage = requiredStage;
            requiredStage = RoundStage.NONE;
            System.out.println("River is open. Shared cards are " + sharedCards);
        } else if (requiredStage.equals(RoundStage.SHOWDOWN)) {
            System.out.println("Card is " + card + ". Waiting " + table.currentPlayer().getName() + " to open hand");
            burnedCards.add(card);
        }
        if (requiredStage.equals(RoundStage.NONE)) {
            switchToNextVoterFrom(table.getDealer());
        }
    }

    public void hide(Card card) {
        burnedCards.add(card);
    }

    private void addRank(Player player, Combo combo) {
        ranks.put(player, combo);
        if (combo.rank > 0) {
            System.out.println(player.getName()
                    + " got " + combo.name + ": " + combo.hand
                    + " with high card " + combo.highCard + " ranked " + combo.rank);
        }
        if (ranks.size() + foldedPlayers.size() == roundBets.size()) {
            stop();
        } else {
            table.setCurrentPlayer(table.getNextPlayerFrom(player));
            while (foldedPlayers.contains(table.currentPlayer())) {
                table.setCurrentPlayer(table.getNextPlayerFrom(table.currentPlayer()));
            }
            System.out.println(table.currentPlayer().getName() + " 's turn to open hand");
        }
    }

    public void rank(Player player, Hand hand) {
        if (!stage.equals(RoundStage.SHOWDOWN)) throw new RuleViolationException("Cannot rank hand on stage " + stage);
        if (!table.currentPlayer().equals(player))
            throw new RuleViolationException("Not " + player.getName() + " turn to open hand");
        HashSet<Card> fullHand = new HashSet<>();
        fullHand.addAll(sharedCards);
        fullHand.addAll(hand);
        addRank(player, HandRanking.calcCombo(fullHand));
    }

    public Stack withdrawWithChange(Stack pot, int sum) {
        Stack part = new Stack();
        int withdrawable = sum;
        while ((withdrawable > 0) && (part.getSum() < sum)) {
            try {
                part.deposit(pot.withdraw(withdrawable));
            } catch (BadConditionException e) {
                withdrawable -= smallestChip.getValue();
            }
        }
        if (withdrawable < sum) {
            pot.deposit(game.getCasino().change(pot.withdrawAll()));
            part.deposit(pot.withdraw(sum - withdrawable));
        }
        return part;
    }

    public Stack splitPot(Stack splitPot, HashMap<Player, Combo> winners) {
        String potType = (splitPot.equals(pot)) ? (winners.size() > 1) ? "Split pot" : "Pot"
                : (winners.size() > 1) ? "Split side pot" : "Side pot";
        int remain = 0;
        while ((splitPot.getSum() - remain) % winners.size() != 0) {
            remain += smallestChip.getValue();
        }
        int winSum = (splitPot.getSum() - remain) / winners.size();
        if (winSum > 0) {
            for (Map.Entry<Player, Combo> winner : winners.entrySet()) {
                winner.getKey().takeChips(withdrawWithChange(splitPot, winSum));
                System.out.println(potType + " " + winSum + " goes to " + winner.getKey().getName());
            }
        }
        return splitPot;
    }

    public void stop() {
        System.out.println("Round ending");
        roundIsOver = true;
        HashMap<Player, Combo> challengers = new HashMap<>();
        for (Map.Entry<Player, Combo> entry : ranks.entrySet()) {
            if (entry.getValue().rank > 0) challengers.put(entry.getKey(), entry.getValue());
        }
        Stack potRemain = new Stack();
        while (!challengers.isEmpty()) {
            HashMap<Player, Combo> winners = new HashMap<>();
            int maxRank = 0;
            for (Map.Entry<Player, Combo> entry : challengers.entrySet()) {
                if (entry.getValue().rank > maxRank) maxRank = entry.getValue().rank;
            }
            for (Map.Entry<Player, Combo> rank : challengers.entrySet()) {
                if (rank.getValue().rank == maxRank) {
                    // TODO calc kickers
                    winners.put(rank.getKey(), rank.getValue());
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
                        potRemain.addAll(splitPot(sidePots.get(player), winners));
                        it.remove();
                        if (player == winner) {
                            challengers.remove(winner);
                            break;
                        }
                    }

                }
            } else {
                Iterator<Player> it = sidePots.keySet().iterator();
                while (it.hasNext()) {
                    Player player = it.next();
                    potRemain.addAll(splitPot(sidePots.get(player), winners));
                    it.remove();
                }
                potRemain.addAll(splitPot(pot, winners));
                winners.forEach((player, combo) -> challengers.remove(player));
            }
            winners.clear();
        }
        System.out.println("Chips remained in pot " + potRemain);
        table.setCurrentPlayer(table.getNextActivePlayerFrom(table.getDealer()));
        table.setDealer(table.getDealer().giveDeck(), table.currentPlayer());
    }

}
