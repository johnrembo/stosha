package ru.rembo.bot.telegram.holdem;

import java.util.EnumSet;

public enum HoldemEvent {
    NEW_PLAYER,
    JOIN_PLAYER,
    BUY_CHIPS,
    GO_AWAY,
    COME_BACK,
    GIVE_DECK,
    SHUFFLE_DECK,
    DEAL,
    BET,
    CHECK,
    CALL,
    RAISE,
    FOLD,
    ALL_IN,
    SHOW_FLOP,
    SHOW_TURN,
    SHOW_RIVER;

    public static EnumSet<HoldemEvent> requireRound() {
        return EnumSet.of(BET, CHECK, CALL, RAISE, FOLD, ALL_IN, SHOW_FLOP, SHOW_TURN, SHOW_RIVER);
    }

}
