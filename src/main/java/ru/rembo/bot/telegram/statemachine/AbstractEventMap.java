package ru.rembo.bot.telegram.statemachine;

import java.util.HashMap;

public abstract class AbstractEventMap<T extends Enum<T>>  extends HashMap<T, Runnable> implements EventMap<T> {

    public abstract void initEventMap();

}
