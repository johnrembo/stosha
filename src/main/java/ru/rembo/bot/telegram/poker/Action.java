package ru.rembo.bot.telegram.poker;

import java.util.EnumSet;

public enum Action {
    CHECK,
    CALL,
    BET,
    RAISE,
    ALL_IN;

    public static EnumSet<Action> continuers() {
        return EnumSet.of(BET, RAISE);
    }

    public static EnumSet<Action> stoppers() {
        return EnumSet.complementOf(continuers());
    }
}
