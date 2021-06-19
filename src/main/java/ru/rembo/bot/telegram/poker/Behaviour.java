package ru.rembo.bot.telegram.poker;

public interface Behaviour<T extends Enum<T>> {

     void behave(T before, T after);

}
