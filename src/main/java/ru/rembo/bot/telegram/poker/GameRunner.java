package ru.rembo.bot.telegram.poker;

public class GameRunner {

    public static void main(String[] args) {
        long chatID = 0;
        Player player1 = new Player("Player One");
        Player player2 = new Player("Player Two");
        Player player3 = new Player("Player Three");
        Player player4 = new Player("Player Four");
        Table table = new Table(chatID);
        Casino casino = new Casino(5);
        Deck deck = new Deck();
        player1.sitAt(table);
        player2.sitAt(table);
        player3.sitAt(table);
        player4.sitAt(table);
        player1.buyChipsFrom(casino, 65);
        player2.buyChipsFrom(casino, 200);
        player3.buyChipsFrom(casino, 100);
        player4.buyChipsFrom(casino, 500);
        Game game  = new Game(casino, table);
        player1.takeDeck(deck);
        player1.shuffleDeck();
        player1.deal(game);
        player2.bet(game, 5);
        player3.bet(game, 10);
        player4.bet(game, 10);
        player1.bet(game, 20);
        player2.askChange(50, casino);
        player2.bet(game, 30);
        player3.fold(game);
        player4.bet(game, 45);
        player1.call(game);
        //player1.allIn(game);
        player2.askChange(100, casino);
        player2.askChange(50, casino);
        player2.call(game);
        player4.askChange(100, casino);
        player4.call(game);
        player1.burnCard(game);
        player1.openFlop(game);
        player2.bet(game, 10);
        player4.call(game);
        player1.call(game);
        player2.check(game);
        player1.burnCard(game);
        player1.openCard(game);
        player2.check(game);
        player4.check(game);
//        player1.check(game);
        player1.burnCard(game);
        player1.openCard(game);
        player2.check(game);
        player4.check(game);
//        player4.check(game);
//        player1.check(game);
        player1.openHand(game);
        player2.openHand(game);
        player4.discard(game);
        player2.showCard(game);
        player2.showCard(game);
        // TODO go away
    }
}
