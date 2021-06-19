package ru.rembo.bot.telegram.poker;

import java.util.EnumSet;

public enum PlayerState {
    OUT_OF_CHIPS,
    LOADED,
    SPECTATOR,
    DEALER,
    SHUFFLED,
    DEALING,
    SMALL_BLIND,
    BIG_BLIND,
    IN_LINE,
    IN_TURN,
    BETTING,
    SHOW_FLOP,
    SHOW_TURN,
    SHOW_RIVER,
    SHOWDOWN,
    RANKED,
    DISCARDED,
    COLLECT_CARDS,
    ALL_IN,
    FOLDED,
    AWAY;

    public static EnumSet<PlayerState> playing() {
        return EnumSet.of(IN_LINE, SMALL_BLIND, BIG_BLIND, DEALER, ALL_IN);
    }

    public static EnumSet<PlayerState> awayOrOutOfChips() {
        return EnumSet.of(AWAY, OUT_OF_CHIPS);
    }

    public static EnumSet<PlayerState> active() {
        return EnumSet.of(IN_LINE, SMALL_BLIND, BIG_BLIND, DEALER);
    }

    public static EnumSet<PlayerState> inTurn() {
        return EnumSet.of(IN_TURN, SMALL_BLIND, BIG_BLIND);
    }

    public static EnumSet<PlayerState> openOrFolded() {
        return EnumSet.of(RANKED, FOLDED, DISCARDED);
    }
}
