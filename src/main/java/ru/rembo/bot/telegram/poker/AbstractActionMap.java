package ru.rembo.bot.telegram.poker;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractActionMap<T extends Enum<T>> extends HashMap<Transition<T>, Runnable> implements ActionMap<T> {

    // TODO key uniqueness check

    AbstractActionMap(Behaviour<T> behaviour) {
        init(behaviour);
    }

    public boolean containsKey(Transition<T> o) {
        for (Transition<T> transition : keySet()) {
            if (transition.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public Runnable put(Transition<T> key, Runnable value) {
        for (Map.Entry<Transition<T>, Runnable> entry : entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.setValue(value);
            }

        }
        return super.put(key, value);
    }

    public Runnable get(Transition<T> key) {
        for (Map.Entry<Transition<T>, Runnable> entry : entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void run(Transition<T> key) {
        get(key).run();
    }

}
