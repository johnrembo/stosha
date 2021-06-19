package ru.rembo.bot.telegram.poker;

public interface Transition<T extends Enum<T>> {

    T getBefore();

    T getAfter();

    boolean equals(Transition<T> o);
}
