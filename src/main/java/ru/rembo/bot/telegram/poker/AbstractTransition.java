package ru.rembo.bot.telegram.poker;

public abstract class AbstractTransition<T extends Enum<T>> implements Transition<T> {

    private final T before;
    private final T after;

    AbstractTransition(T before, T after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public T getBefore() {
        return before;
    }

    @Override
    public T getAfter() {
        return after;
    }

    @Override
    public boolean equals(Transition<T> transition) {
        return transition.getBefore().equals(before) && transition.getAfter().equals(after);
    }
}
