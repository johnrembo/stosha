package ru.rembo.bot.telegram.poker;

public class GameRunner {

    public static void main(String[] args) {
        long chatID = 0;
        Player player1 = new Player("Player One");
        Player player2 = new Player("Player Two");
        Player player3 = new Player("Player Three");
        Player player4 = new Player("Player Four");
        Player player5 = new Player("Player Five");
        Table table = new Table(chatID);
        Casino casino = new Casino(5);
        player1.changeChips(casino, 65);
        player2.changeChips(casino, 200);
        player3.changeChips(casino, 100);
        player4.changeChips(casino, 500);
        Deck deck = new Deck();
        Game game  = new Game(casino, table, deck);
        game.join(player1, game);
        game.join(player2, game);
        game.join(player3, game);
        game.join(player4, game);
//        game.changeState(player5, PlayerState.SPECTATOR, game);
//        game.changeState(player5, PlayerState.AWAY);
//        game.changeState(player5, PlayerState.SPECTATOR);
        game.giveDeck(player1, deck);
        game.shuffleDeck(player1);
        game.doDeal(player1, table);
//        game.changeState(player1, PlayerState.IN_LINE);
//        player2.bet(game, 5);
        game.doBet(player2, 5);
        game.doBet(player3, 10);

        game.doBet(player4, 10);
        game.doBet(player1, 20);
        player2.askChange(50, casino);
        game.doBet(player2, 30);
        game.doFold(player3);
        game.doBet(player4, 50);
        game.doCall(player1);
        player2.askChange(100, casino);
        player2.askChange(50, casino);
        game.doCall(player2);
        player4.askChange(100, casino);
        game.doCall(player4);
//        player1.burnCard(game);
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
//        player2.showCard(game);
//        player2.showCard(game);

 //       player3.actTo(PlayerState.AWAY);


    }


}
