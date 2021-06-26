package ru.rembo.bot.telegram.statemachine;

import java.util.HashSet;

public interface EventHandler<T, S> {

    String getHandlerIdentifier();

    boolean handles(T event);

    void handle(T event);

    S getGlobalAnswer(T event);

    void clearGlobalAnswer(T event);

    HashSet<S> getBulkAnswer(T event);

    void clearBulkAnswer(T event);
}
