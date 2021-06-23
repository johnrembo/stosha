package ru.rembo.bot.telegram.statemachine;

import java.util.Map;

public interface EventMap<T extends Enum<T>> extends Map<T, Runnable> {

}
