package ru.rembo.bot.telegram.poker;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Table extends ArrayList<Player> {

    private final long chatID;
    private static final int MAX_PLAYERS = 8;
    private int dealer;

    public Table(long chatID) {
        super();
        this.chatID = chatID;
    }

    public long getID() {
        return chatID;
    }

    public Player getDealer() {
        return get(this.dealer);
    }

    public void setDealer(Player dealer) {
        this.dealer = indexOf(dealer);
    }

    public int getNextPlayerFrom(int index) {
        if ((index < 0) || (index >= size())) throw new BadConditionException("Bad player index: " + index);
        int i = index;
        while (++i != index) {
            if (i == size()) i = 0;
            if (get(i).isPlaying()) break;
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
            if (get(i).canAct()) break;
        }
        return i;
    }

    public Player getNextActivePlayerFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextActivePlayerFrom(currentIndex));
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

    public Player getPlayerByState(PlayerState state) {
//        Stream<Player> playerStream = stream().filter(player -> player.getState().equals(state));
        if (stream().filter(player -> player.getState().equals(state)).count() > 1) throw new BadConditionException("Player state " + state + " is not unique");
        return stream().filter(player -> player.getState().equals(state)).findFirst().orElse(null);
    }

    public Player getSmallBlind() {
        return getPlayerByState(PlayerState.SMALL_BLIND);
    }

    public Player getBigBlind() {
        return getPlayerByState(PlayerState.BIG_BLIND);
    }

    public Player getBettingPlayer() {
        return getPlayerByState(PlayerState.BETTING);
    }

    public Player getPlayerInTurn() {
        Stream<Player> playerStream = stream().filter(player -> PlayerState.inTurn().contains(player.getState()));
        if (playerStream.count() > 0)  throw new BadConditionException("More than one acting player");
        return playerStream.findFirst().orElse(null);
    }

    public boolean allPlayersAreOpen() {
        return stream().filter(player -> (PlayerState.openOrFolded().contains(player.getState()))).count() == size();
    }
}
