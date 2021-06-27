package ru.rembo.bot.telegram.statemachine;

import ru.rembo.bot.telegram.GlobalProperties;

import java.util.HashMap;
import java.util.Locale;

public class AbstractParsedEvent<T extends Enum<T>, S> implements ParsedEvent<T> {

    protected T eventEnum;
    protected HashMap<String, String> args;
    protected final S event;
    protected Locale locale;
    protected String key;
    protected String outputString;


    public AbstractParsedEvent(S event) {
        this.event = event;
    }

    @Override
    public T getEvent() {
        return eventEnum;
    }

    @Override
    public HashMap<String, String> getArgs() {
        return args;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getOutputString(Object... args) {
        return String.format(outputString, args);
    }

}
