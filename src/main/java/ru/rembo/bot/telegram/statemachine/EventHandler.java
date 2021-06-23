package ru.rembo.bot.telegram.statemachine;

public interface EventHandler<V> {
    Runnable getAction(V obj);

    boolean handles(V obj);

    V getPrivateAnswer();

    V getGlobalAnswer();

    V getHandlerIdentifier();
}
