package ru.rembo.bot.telegram.holdem;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.statemachine.AbstractParsedEvent;

import java.util.Arrays;

public class HoldemParsedEvent extends AbstractParsedEvent<HoldemEvent, Message> {

    private final int id;
    private final String name;

    public HoldemParsedEvent(Message message) {
        super(message);
        id = message.getFrom().getId();
        name = (message.getFrom().getUserName() == null) ? message.getFrom().getFirstName() : message.getFrom().getUserName();
        GlobalProperties.InputPatternMatch match = GlobalProperties.matchInput(message);
        if ((match != null) && match.key.contains("HoldemEvent.")) {
            key = match.key;
            eventEnum = HoldemEvent.valueOf(match.key.substring(12));
            args = match.args;
            locale = match.locale;
            String[] messages =  GlobalProperties.outputMessages.get(locale).getString(key).split(";");
            outputString = messages[(int) (Math.random() * (Arrays.stream(messages).count() - 1))];
        }
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return event.getText();
    }

}
