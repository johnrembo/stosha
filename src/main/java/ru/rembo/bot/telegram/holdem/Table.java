package ru.rembo.bot.telegram.holdem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class Table extends ArrayList<Player> {

    private static final int MAX_PLAYERS = 8;
    private final HashMap<Integer, Integer> playersById = new HashMap<>();
    private int dealer = -1;

    public Table() {
        super();
    }

    public Player getDealer() {
        if ((dealer < 0) || (dealer >= size())) return null;
        return get(dealer);
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

    public long readyPlayerCount() {
        return stream().filter(Player::canPlay).count();
    }

    public void addPlayer(Player player) {
        if (size() == MAX_PLAYERS)
            throw new RuleViolationException("MAX_PLAYERS", MAX_PLAYERS);
        if (this.contains(player))
            throw new RuleViolationException("PLAYER_EXISTS", player.getName());
        add(player);
        playersById.put(player.getId(), indexOf(player));
        System.out.println(player.getName() + " joins table");
    }

    public void removePlayer(Player player) {
        if (!this.contains(player))
            throw new RuleViolationException("PLAYER_NOT_EXISTS", player.getName());
        remove(player);
        playersById.remove(player.getId());
        System.out.println(player.getName() + " leaves table");
    }

    public long challengerCount() {
        return stream().filter(Player::inChallenge).count();
    }

    public Player getNextChallengingFrom(Player player) {
        int currentIndex = this.indexOf(player);
        if (currentIndex == -1) throw new BadConditionException("No such player on table: " + player.getName());
        return get(getNextPlayerFrom(currentIndex, Player::inChallenge));
    }

    public Player getById(int id) {
        return get(playersById.get(id));
    }

    public boolean containsPlayer(int id) {
        return playersById.containsKey(id);
    }

}
