package ru.rembo.bot.telegram.statemachine;

/**
 * Interface describes actual {@link Behaviour} bearer state machine
 * described by an {@link ActionMap}.
 *
 * Object implementing {@code ActorBehaviour} is said to be acting (behaving) as one
 * or is an actor handling set of {@link Transition} events (finite state machine - FSM).
 *
 * @param <T> - type of enum state specific to implementing actor
 */
public interface ActorBehaviour<T extends Enum<T>> extends Behaviour<T> {

    /**
     * Current state of an actor instance.
     *
     * @return - one of enum states. Never returns null
     */
    T getState();

    /**
     * Initial state should be provided in class constructor or {@code Singleton} object initializer.
     * {@code initState} has to check if object state is already set and ignore new {@code state}, or throw an
     * exception. {@code null} argument must throw {@link NullPointerException}
     *
     * @param state - initial enum state of an FSM.
     */
    void initState(T state);

    ActionMap<T> getActionMap();

    /**
     * Initialize immutable behaviour {@link ActionMap} if actor class has static methods.
     *
     * @param actions - behavior map that can be described by static methods of an actor class
     */
    void initActions(ActionMap<T> actions);

    /**
     * Call {@link Behaviour#behave(T before, T after)} assuming {@code before} state to be
     * equal to {@code this.getBefore()} result and set current state to {@code newState}
     * so that {@code this.getState()} returns {@code newState} after action has been
     * executed.
     *
     * @param newState - new state of an actor
     */
    void actTo(T newState);

}
