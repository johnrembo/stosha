package ru.rembo.bot.telegram.statemachine;

/**
 * Finite state machine (FSM) single {@code Transition} event.
 *
 * Used as a key in {@link ActionMap} to behave as an object that implements {@link Behaviour}
 *
 * @param <T> - type of enum state
 */

public interface Transition<T extends Enum<T>> {

    /**
     * Enumerable state {@code before} event
     */
    T getBefore();

    /**
     * Enumerable state {@code after} event
     */
    T getAfter();

    /**
     * Transitions are treated the same when both {@code getBefore()} and {@code getBefore()} results are equal
     * as two {@link Enum<T>} objects accurate to class. However {@code Transition} objects may themselves be of
     * different instances.
     *
     * @param o - other {@code Transition} class object compared to {@code this}
     * @return - {@code true} if {@code this.getBefore() == o.getBefore()}
     * and {@code this.getAfter() == o.getAfter()}
     */
    boolean equals(Transition<T> o);
}
