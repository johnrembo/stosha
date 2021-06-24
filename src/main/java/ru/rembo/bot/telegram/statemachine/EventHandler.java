package ru.rembo.bot.telegram.statemachine;

public interface EventHandler<V> {

    boolean handles(V obj);

    void handle(V obj);

    V getPrivateAnswer();

    V getGlobalAnswer();

    V getHandlerIdentifier();
}
