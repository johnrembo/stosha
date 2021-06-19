package ru.rembo.bot.telegram.poker;

import java.util.Map;
public interface ActionMap<T extends Enum<T>> extends Map<Transition<T>, Runnable> {

    void init(Behaviour<T> behaviour);

    boolean containsKey(Transition<T> o);

    Runnable put(Transition<T> key, Runnable value);

    Runnable get(Transition<T> key);

    void run(Transition<T> key);
}
