package ru.rembo.bot.telegram.poker;

public class Player extends Dealer {
    private String name;
    private Stack stack = new Stack();
    private Hand hand = new Hand();
    private boolean away = false;

    public Player(String name) {
        this.name = name;
    }

    public boolean hasChips() {
        return stack.size() > 0;
    }

    public boolean hasChip(Chip chip) {
        return stack.contains(chip);
    }

    public int stackSum() {
        return stack.getSum();
    }

    public boolean isAway() {
        return away;
    }

    public void goAway() {
        away = true;
    }

    public void getBack() {
        away = false;
    }

    public String getName() {
        return name;
    }

    public void sitAt(Table table) {
        table.addPlayer(this);
    }

    public void buyChipsFrom(Casino casino, int sum) {
        stack.deposit(casino.change(sum));
        System.out.println(name + " buys chips for " + sum);
    }

    public void askChange(int sum, Casino casino) {
        Stack part = stack.withdraw(sum);
        System.out.println(name + " asks to change chips from bank: " + part);
        stack.deposit(casino.change(part));
    }

    public void bet(Game game, int sum) {
        Stack part = stack.getPart(sum);
        if (sum == 0) {
            System.out.println(name +  " checks");
        } else {
            System.out.println(name +  " bets " + sum);
        }
        game.stake(this, part);
    }

    public Stack give(Stack part) {
        if (!part.isEmpty()) {
            System.out.println(name + " puts " + part + " in pot");
        }
        return stack.withdraw(part);
    }

    public void fold(Game game) {
        System.out.println(name + " is folding");
        game.fold(this);
    }

    public void call(Game game) {
        bet(game, game.getCallAmount() - game.getPlayerRoundStack(this).getSum());
    }

    public void allIn(Game game) {
        System.out.println(name + " goes all in!");
        bet(game, stack.getSum());
    }

    public void check(Game game) {
        bet(game, 0);
    }

    // TODO raise

    public void takeCard(Card card) {
        hand.add(card);
    }

    public boolean hasCards() {
        return !hand.isEmpty();
    }

    public boolean isActive() {
        return (isAway() && hasChips());
    }

    public void openHand(Game game) {
        System.out.println(name + " hand is " + hand);
        game.rankHand(this, hand);
    }

    public void discard(Game game) {
        System.out.println(name + " discards hand");
        game.skipRank(this);
    }

    public void takeChips(Stack chips) {
        System.out.println(name + " gets " + chips.getSum());
        stack.deposit(chips);
    }

    // TODO pass chips to other players
}
