package ru.rembo.bot.telegram.poker;

public interface ActorBehaviour<T extends Enum<T>> extends Behaviour<T> {

    T getState();

    void initState(T state);

    ActionMap<T> getActionMap();

    void initActions(ActionMap<T> actions);

    void actTo(T newState);

}
