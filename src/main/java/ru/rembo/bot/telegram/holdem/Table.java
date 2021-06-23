package ru.rembo.bot.telegram.holdem;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Table extends ArrayList<Player> {

    private final long chatID;
    private static final int MAX_PLAYERS = 8;
    private final String name;
    private int dealer;

    public Table(long chatID, String name) {
        super();
        this.chatID = chatID;
        this.name = name;
    }

    public long getID() {
        return chatID;
    }

    public String getName() {
        return (name != null) ? name : String.valueOf(getID());
    }
    public Player getDealer() {
        return get(this.dealer);
    }

    public void setDealer(Player dealer) {
        this.dealer = indexOf(dealer);
    }

    public int getNextPlayerFrom(int index, Predicate<Player> predicate) {
        if ((index < 0) || (index >= size())) throw new BadConditionException("Bad player index: " + index);
        int i = (index == size() - 1) ? 0 : index + 1;
        while (i != index) {
            if (predicate.test(get(i))) break;
            i++;
            if (i == size()) i = 0;
        }
        return i;
    }

    public Player getNextPlayingFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex, Player::isPlaying));
    }

    public Player getNextPlayerFrom(Player player, Predicate<Player> predicate) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex, predicate));
    }

    public Player getNextActivePlayerFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex, Player::canAct));
    }

    public long countByState(PlayerState state) {
        return stream().filter(player -> player.getState().equals(state)).count();
    }

    public long activePlayerCount() {
        return stream().filter(Player::canAct).count();
    }

    public void addPlayer(Player player) {
        if (size() == MAX_PLAYERS)
            throw new BadConditionException("Maximum number of players (" + MAX_PLAYERS + ") reached");
        if (this.contains(player))
            throw new RuleViolationException(player.getName() + " is already on table " + chatID);
        add(player);
        System.out.println(player.getName() + " joins table " + chatID);
    }

    public long challengerCount() {
        return stream().filter(Player::inChallenge).count();
    }

    public Player getNextChallengingFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex, Player::inChallenge));
    }

}