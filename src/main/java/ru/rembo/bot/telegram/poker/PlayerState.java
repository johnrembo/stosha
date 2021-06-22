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
    ALL_IN,
    FOLDED,
    SHOW_FLOP,
    SHOW_TURN,
    SHOW_RIVER,
    SHOWDOWN,
    OPTIONAL_SHOWDOWN,
    RANKED,
    RANKED_HIDDEN,
    COLLECT_CARDS,
    RETURN_DECK,
    SHOW_FROM_TOP,
    AWAY;

    public static EnumSet<PlayerState> playing() {
        return EnumSet.of(IN_LINE, SMALL_BLIND, BIG_BLIND, DEALER, ALL_IN, FOLDED, RANKED);
    }

    public static EnumSet<PlayerState> awayOrOutOfChips() {
        return EnumSet.of(AWAY, OUT_OF_CHIPS);
    }

    public static EnumSet<PlayerState> active() {
        return EnumSet.of(IN_LINE, SMALL_BLIND, BIG_BLIND, DEALER);
    }

    public static EnumSet<PlayerState> challenging() {
        return EnumSet.of(IN_LINE, SMALL_BLIND, BIG_BLIND, DEALER, ALL_IN);
    }
}
