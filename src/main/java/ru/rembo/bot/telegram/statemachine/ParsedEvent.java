package ru.rembo.bot.telegram.statemachine;

public interface ParsedEvent <T extends Enum<T>> {

    T getEvent();

    String[] getArgs();

}
