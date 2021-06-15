package ru.rembo.bot.telegram.poker;

import java.util.ArrayList;

public class Table extends ArrayList<Player> {

    private long chatID;
    private int current = 0;
    private int dealer = 0;
    private int smallBlind = 0;
    private int bigBlind = 0;
    private static final int MAX_PLAYERS = 8;
    private Stack bank = new Stack();

    public Table(long chatID) {
        super();
        this.chatID = chatID;
    }

    public Player currentPlayer() {
        return get(current);
    }

    public int getNextPlayerFrom(int index) {
        if ((index < 0) || (index >= size())) throw new BadConditionException("Bad player index: " + index);
        int i = index;
        while (++i != index) {
            if (i == size()) i = 0;
            if (get(i).isAway()) break;
        }
        return i;
    }

    public Player getNextPlayerFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex));
    }

    public int getNextActivePlayerFrom(int index) {
        if ((index < 0) || (index >= size())) throw new BadConditionException("Bad player index: " + index);
        int i = index;
        while (++i != index) {
            if (i == size()) i = 0;
            if (get(i).isActive()) break;
        }
        return i;
    }

    public Player getNextActivePlayerFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextActivePlayerFrom(currentIndex));
    }

    public void setCurrentPlayer(int index) {
        if ((index < 0) || (index >= size())) throw new BadConditionException("Bad player index: " + index);
        current = index;
    }
    public void setCurrentPlayer(Player player) {
        if (indexOf(player) == -1) throw new BadConditionException("No such player on table: " + player.getName());
        setCurrentPlayer(indexOf(player));
    }

    public void switchToNextActivePlayer() {
        setCurrentPlayer(getNextActivePlayerFrom(currentPlayer()));
    }

    public int playerCount() {
        return size();
    }

    public long activePlayerCount() {
        return stream().filter(Player::isActive).count();
    }

    public void addPlayer(Player player) {
        if (size() == MAX_PLAYERS) {
            throw new BadConditionException("Maximum number of players (" + MAX_PLAYERS + ") reached");
        }
        if (this.contains(player)) {
            throw new BadConditionException(player.getName() + " is already on table " + chatID);
        }
        this.add(player);
        System.out.println(player.getName() + " sits at table " + chatID);
    }

    public void setDealer(Deck deck, int index) {
        dealer = index;
        System.out.println(getDealer().getName() + " is dealer");
        get(index).takeDeck(deck);
        smallBlind = getNextActivePlayerFrom(index);
        System.out.println(getSmallBlind().getName() + " is on small blind");
        bigBlind = getNextActivePlayerFrom(smallBlind);
        System.out.println(getBigBlind().getName() + " is on big blind");
        current = smallBlind;
    }

    public void setDealer(Dealer dealer) {
        setDealer(dealer.deck, indexOf(dealer));
    }

    public void setDealer(Deck deck, Player player) {
        setDealer(deck, indexOf(player));
    }

    public Player getDealer() {
        return get(dealer);
    }

    public Player getSmallBlind() {
        return get(smallBlind);
    }

    public Player getBigBlind() {
        return get(bigBlind);
    }

}
