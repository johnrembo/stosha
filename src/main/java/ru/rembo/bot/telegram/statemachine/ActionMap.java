package ru.rembo.bot.telegram.statemachine;

import java.util.Map;

/**
 * Finite state machine (FSM) transition map object
 *
 * Describes transition behavior of the object when object changes it's state.
 * {@link Behaviour#behave(T before, T after)} method accesses {@code ActionMap} by unique {@link Transition} key
 * and runs corresponding {@link Runnable} instance.
 * Because {@link Transition} equality is not restricted to instance implementing class
 * should itself control uniqueness of key set. Thus has to at least override
 * {@link Map#containsKey(Object key)}, {@link Map#get(Object key)} and {@link Map#put(Object key, Object value)}
 * methods.
 * {@code ActionMap} instance is initialized in an {@link ActorBehaviour} class in its
 * {@code static} block or in the constructor. It is a bad practice to juggle state machine
 * behavior after it has been initialized. Neither it is a good idea to change transition event
 * methods dynamically. When such abuse occurs state machine ceases to be "Deterministic".
 */
public interface ActionMap<T extends Enum<T>> extends Map<Transition<T>, Runnable> {

    /**
     * {@code Init} is called in static block of an {@link ActorBehaviour} class to
     * provide behaviour described by static methods of an actor class.
     * Should be overridden on implementation declaration. Can be empty.
     *
     * @param behaviour - {@link ActorBehaviour} class instance ({@code this} at most)
     */
    void init(Behaviour<T> behaviour);

    /**
     * Search key that satisfies {@link Transition#equals(Transition o)} method implementation.
     * When multiple occurrences of searched key found may throw {@link IllegalArgumentException}
     *
     * @param o - searched {@link Transition} object
     * @return - {@code true} if key found and unique, {@code false} - when not found
     */
    boolean containsKey(Transition<T> o);

    /**
     * Implementation of {@link Map#put(Object key, Object value)} method with local {@code get} method call
     * to find current associated element.
     *
     * @param key - {@link Transition} object
     * @param value - {@link Runnable} instance that will be executed by {@link Behaviour#behave(T before, T after)}
     *              method on state change. New state will be set after {@link Runnable#run()}
     *              successfully finished execution.
     * @return - the previous value associated with key, or null if there was no mapping for key.
     */
    Runnable put(Transition<T> key, Runnable value);

    /**
     * Implementation of {@link Map#get(Object key)} method with local {@code contains} method call to check
     * if key exists and unique.
     *
     * @param key - {@link Transition} object
     * @return - the {@link Runnable} to which the specified key is mapped, or null if this map contains
     * no mapping for the key
     */
    Runnable get(Transition<T> key);

    /**
     * Alias to {@code this.get(key).run()} with key uniqueness control
     *
     * @param key - {@link Transition} object
     */
    void run(Transition<T> key);
}
