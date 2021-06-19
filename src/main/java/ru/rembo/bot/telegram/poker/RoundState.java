package ru.rembo.bot.telegram.poker;

public enum RoundState {
    STARTED,
    TAKE_BET,
    SMALL_BLIND,
    BIG_BLIND,
    PREFLOP,
    WAIT_FLOP,
    FLOP,
    WAIT_TURN,
    TURN,
    WAIT_RIVER,
    RIVER,
    SHOWDOWN,
    RANK,
    DISCARD,
    CHOP_THE_POT,
    OVER
}
