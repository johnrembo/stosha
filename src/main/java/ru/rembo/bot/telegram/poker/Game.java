package ru.rembo.bot.telegram.poker;

public class Game {
    private final Table table;
    private final Casino casino;
    private Round round;
    private int smallBlindAmount = 5, bigBlindAmount = 10;
    private boolean over = false;

    public Game(Casino casino, Table table) {
        this.table = table;
        this.casino = casino;
        System.out.println("Game started with " + table.playerCount() + " players. Blinds are "
                + smallBlindAmount + ", " + bigBlindAmount);
    }

    public Table getTable() {
        if (isOver()) throw new BadConditionException("Game is over");
        return table;
    }

    public Casino getCasino() {
        return casino;
    }

    public int getCallAmount() {
        return round.getCallAmount();
    }

    public Stack getPlayerRoundStack(Player player) {
        return round.getPlayerStack(player);
    }

    public boolean isOver() {
        return over;
    }

    public void stop() {
        System.out.println("Ending game");
        this.over = true;
    }

    public void stake(Player betMaker, Stack chips) {
        round.stake(betMaker, chips);
    }

    public int getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public int getBigBlindAmount() {
        return bigBlindAmount;
    }

    public void setBlinds(int smallBlindAmount, int bigBlindAmount) {
        if (isOver()) throw new BadConditionException("Game is over");
        if ((round != null) && !round.isOver())
            throw new RuleViolationException("Round is running. Wait until it ends");
        if ((smallBlindAmount % casino.getSmallestChip().getValue() != 0)
                || (bigBlindAmount % casino.getSmallestChip().getValue() != 0))
            throw new BadConditionException("No such chips to represent blind. Must be divisible by "
                    + casino.getSmallestChip().getValue());
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        System.out.println("Blind is set to " + smallBlindAmount + ", " + bigBlindAmount);
    }

    public void fold(Player player) {
        round.fold(player);
    }

    public void exposeCard(Card card) {
        if ((round == null) || round.isOver()) {
            System.out.println(card);
        } else {
            round.turnOver(card);
        }
    }

    public void disposeCard(Card card) {
        round.hide(card);
    }

    public void rankHand(Player player, Hand hand) {
        round.rank(player, hand);
    }

    public void skipRank(Player player) {
        round.rank(player, new Hand());
    }

    public void newRound(Dealer dealer) {
        round = new Round(this, dealer);
    }

    public Chip getSmallestChip() {
        return casino.getSmallestChip();
    }
}
