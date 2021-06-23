package ru.rembo.bot.telegram.holdem;

public class GameRunner {

    public static void main(String[] args) {
        long chatID = 0;
        for (int i = 0; i < 1; i++) {
            Player player1 = new Player("Player One");
            Player player2 = new Player("Player Two");
            Player player3 = new Player("Player Three");
            Player player4 = new Player("Player Four");
            Player player5 = new Player("Player Five");
            Table table = new Table(chatID, "test");
            Casino casino = new Casino(5);
            Deck deck = new Deck();
            Game game = new Game(casino, table);
            game.join(player1);
            game.join(player2);
            game.join(player3);
            game.join(player4);
            player1.cashIn(casino, 65);
            player2.cashIn(casino, 200);
            player3.cashIn(casino, 100);
            player4.cashIn(casino, 500);
            player5.cashIn(casino, 5);
            game.giveDeck(player1, deck);
            game.shuffleDeck(player1);
            game.doDeal(player1, table);
            game.doBet(player2, 5);
            game.doBet(player3, 10);
            game.doBet(player4, 10);
            game.doBet(player1, 20);
            player2.exchange(50, casino);
            game.doBet(player2, 30);
            game.doFold(player3);
            game.doBet(player4, 50);
            game.doCall(player1);
            player2.exchange(100, casino);
            player2.exchange(50, casino);
            game.doCall(player2);
            player4.exchange(100, casino);
            game.doCall(player4);
            game.doShowFlop(player1);
            game.doBet(player2, 10);
            game.doCall(player4);
            game.doAllIn(player1);
            game.doCheck(player2);
            game.doShowTurn(player1);
            game.doCheck(player2);
            game.doCheck(player4);
            game.doCheck(player1);
            game.doShowRiver(player1);
            game.doCheck(player2);
            game.doCheck(player4);
            game.doCheck(player1);
            game.doRank(player1);
            game.doRank(player2);
            game.doDiscard(player4);
            game.doShowCard(player2);
            game.doShowCard(player2);
            game.doGoAway(player3);
            player1.cashOut(casino);
            game.setBlinds(10, 25);
            game.doDeal(player2, table);
            game.doRaise(player4);
            game.doRaise(player2, 100);
            game.doFold(player4);
            game.doHiddenRank(player2);
        }
    }

}