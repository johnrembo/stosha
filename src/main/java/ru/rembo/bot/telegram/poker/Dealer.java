package ru.rembo.bot.telegram.poker;

public class Dealer {
    public Deck deck;
    private boolean cardBurned = false;

    public void shuffleDeck() {
        deck.shuffle();
        System.out.println("Dealer shuffles the deck");
    }

    public void showCard(Game game) {
        if (deck == null) throw new BadConditionException("Player has no deck");
        System.out.println("Dealer shows next card");
        game.exposeCard(deck.pullCard());
    }

    public void burnCard(Game game) {
        if (cardBurned) throw new RuleViolationException("Cannot burn two cards at a time");
        game.disposeCard(deck.pullCard());
        System.out.println("Dealer burns card");
        cardBurned = true;
    }

    public void openCard(Game game) {
        if (!cardBurned) throw new RuleViolationException("Previous card should be burned");
        System.out.println("Dealer opens card");
        game.exposeCard(deck.pullCard());
        cardBurned = false;
    }

    public void openFlop(Game game) {
        if (!cardBurned) throw new RuleViolationException("Previous card should be burned");
        System.out.println("Dealer opens flop");
        for (int i = 0; i < 3; i++) {
            game.exposeCard(deck.pullCard());
        }
        cardBurned = false;
    }

    public void takeDeck(Deck deck) {
        if (this.deck == null) {
            this.deck = deck;
            System.out.println("Dealer takes deck");
        }
    }

    public void deal(Game game) {
        if (deck == null) throw new BadConditionException("Player has no deck");
        if (deck.cardCount() != 52) throw new RuleViolationException("Missing or excessive cards in deck");
        System.out.println("Dealer deals cards");
        for (int i = 0; i < 2; i++) {
            game.getTable().forEach(player -> {
                if (player.hasChips() && player.isAway()) {
                    player.takeCard(deck.pullCard());
                }
            });
        }
        game.newRound(this);
    }

    // TODO collect cards

    public Deck giveDeck() {
        if (deck == null) throw new BadConditionException("Player has no deck");
        Deck result = deck;
        deck = null;
        System.out.println("Dealer gives deck");
        return result;
    }

}
