package ru.rembo.bot.telegram.statemachine;

public interface EventHandler<T, S> {

    boolean handles(T event);

    void handle(T event);

    S getPrivateAnswer();

    S getGlobalAnswer();

    String getHandlerIdentifier();
}
