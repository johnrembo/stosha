package ru.rembo.bot.telegram.holdem;

import java.util.EnumSet;

public enum HoldemEvent {
    HELP,
    NEW_PLAYER,
    JOIN_PLAYER,
    BUY_CHIPS,
    GO_AWAY,
    COME_BACK,
    GIVE_DECK,
    SHUFFLE_DECK,
    DEAL,
    SMALL_BLIND,
    BIG_BLIND,
    BET,
    CHECK,
    CALL,
    RAISE,
    FOLD,
    ALL_IN,
    SHOW_FLOP,
    SHOW_TURN,
    SHOW_RIVER,
    RANK,
    HIDDEN_RANK,
    DISCARD,
    SHOW_CARD,
    ASK_CHANGE,
    SELL_CHIPS;

    public static EnumSet<HoldemEvent> requireRound() {
        return EnumSet.of(BET, CHECK, CALL, RAISE, FOLD, ALL_IN, SHOW_FLOP, SHOW_TURN, SHOW_RIVER);
    }

}
