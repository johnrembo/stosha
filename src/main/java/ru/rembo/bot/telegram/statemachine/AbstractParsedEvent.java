package ru.rembo.bot.telegram.statemachine;

public class AbstractParsedEvent<T extends Enum<T>> implements ParsedEvent<T> {

    protected T eventEnum;
    protected String[] arguments;
    private final String eventText;

    public AbstractParsedEvent(String eventText, String[] initArgs) {
        this.eventText = eventText;
        arguments = initArgs;
    }

    @Override
    public T getEvent() {
        return eventEnum;
    }

    @Override
    public String[] getArgs() {
        return arguments;
    }

    public String getText() {
        return eventText;
    }
}
