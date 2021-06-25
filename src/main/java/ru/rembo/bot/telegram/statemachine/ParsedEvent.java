package ru.rembo.bot.telegram.statemachine;

import java.util.HashMap;
import java.util.Locale;

public interface ParsedEvent <T extends Enum<T>> {

    T getEvent();

    HashMap<String, String> getArgs();

    Locale getLocale();

    String getKey();

    String getOutputString();
}
