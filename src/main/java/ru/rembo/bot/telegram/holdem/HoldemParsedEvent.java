package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.statemachine.AbstractParsedEvent;

import java.util.Locale;

public class HoldemParsedEvent extends AbstractParsedEvent<HoldemEvent> {

    public HoldemParsedEvent(String text, String[] args) {
        super(text, args);
        // TODO use telegram locale
        // TODO parse event

        if (text.contains("здесь")) {
            eventEnum = HoldemEvent.NEW_PLAYER;
        } else if (text.contains("играю")) {
            eventEnum = HoldemEvent.JOIN_PLAYER;
        }
    }
}
