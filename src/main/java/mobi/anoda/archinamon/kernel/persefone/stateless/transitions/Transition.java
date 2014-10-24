package mobi.anoda.archinamon.kernel.persefone.stateless.transitions;

public class Transition<IState, ITrigger> {

    private final IState   source;
    private final IState   destination;
    private final ITrigger trigger;

    /**
     * Construct a transition
     *
     * @param source      The state transitioned from
     * @param destination The state transitioned to
     * @param trigger     The trigger that caused the transition
     */
    public Transition(IState source, IState destination, ITrigger trigger) {
        this.source = source;
        this.destination = destination;
        this.trigger = trigger;
    }

    /**
     * The state transitioned from
     *
     * @return The state transitioned from
     */
    public IState getSource() {
        return source;
    }

    /**
     * The state transitioned to
     *
     * @return The state transitioned to
     */
    public IState getDestination() {
        return destination;
    }

    /**
     * The trigger that caused the transition
     *
     * @return The trigger that caused the transition
     */
    public ITrigger getTrigger() {
        return trigger;
    }

    /**
     * True if the transition is a re-entry, i.e. the identity transition
     *
     * @return True if the transition is a re-entry
     */
    public boolean isReentry() {
        return getSource().equals(getDestination());
    }
}