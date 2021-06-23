package ru.rembo.bot.telegram.statemachine;

/**
 * State machine {@link Transition} event handler interface.
 *
 * Object implementing {@code Behaviour} is an {@link ActionMap} bearer,
 * i.e. object is able to behave in a way described by {@link ActionMap}.
 *
 * @param <T> - type of enumerable state
 *
 * {@see ActorBehaviour}
 */

public interface Behaviour<T extends Enum<T>> {

     /**
      * Act on event that happens when state is changed from {@code before} to {@code after}.
      *
      * @param before Enumerable object state before {@link Transition} event
      * @param after Enumerable object state after {@link Transition} event
      */
     void behave(T before, T after);

}
