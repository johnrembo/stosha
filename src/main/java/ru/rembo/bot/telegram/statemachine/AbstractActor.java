package ru.rembo.bot.telegram.statemachine;

public abstract class AbstractActor<T extends Enum<T>> implements ActorBehaviour<T> {

    private T state;
    private AbstractActionMap<T> actionMap;

    public T getState() {
        return state;
    }

    public void initState(T state) {
        if (state == null) throw new NullPointerException("Actor state must not be 'null'");
        if (this.state == null) this.state = state;
    }

    public ActionMap<T> getActionMap() {
        return actionMap;
    }

    public void initActions(ActionMap<T> actionMap) {
        if (this.actionMap == null) this.actionMap = (AbstractActionMap<T>) actionMap;
    }

    public void actTo(T newState) {
        behave(getState(), newState);
        state = newState;
    }

    public void behave(T before, T after) {
        AbstractTransition<T> transition = new AbstractTransition<T>(before, after) {};
        if (getActionMap().containsKey(transition)) {
            getActionMap().run(transition);
        } else {
            throw new BadStateException("Bad behaviour: " + before + " -> " + after);
        }
    }

    protected void accept() {
        // just accept this
    }

}
