package mobi.anoda.archinamon.kernel.persefone.stateless;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Action1;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Action2;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Func;
import mobi.anoda.archinamon.kernel.persefone.stateless.transitions.Transition;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerBehaviour;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerWithParameters;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerWithParameters1;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerWithParameters2;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerWithParameters3;

/**
 * Models behaviour as transitions between a finite set of states
 *
 * @param <IState>   The type used to represent the states
 * @param <ITrigger> The type used to represent the triggers that cause state transitions
 */
public class StateMachine<IState, ITrigger> {

    private final Logger logger = Logger.getLogger(getClass().getName());
    protected final StateMachineConfig<IState, ITrigger> config;
    protected final Func<IState>                         stateAccessor;
    protected final Action1<IState>                      stateMutator;
    protected Action2<IState, ITrigger> unhandledTriggerAction = new Action2<IState, ITrigger>() {

        @Implement
        public void doIt(IState state, ITrigger trigger) {
            throw new IllegalStateException(String.format("No valid leaving transitions are permitted from state '%s' for trigger '%s'. Consider ignoring the trigger.", state, trigger));
        }
    };

    /**
     * Construct a state machine
     *
     * @param initialState The initial state
     */
    public StateMachine(IState initialState, StateMachineConfig<IState, ITrigger> config) {
        this.config = config;
        final StateReference<IState, ITrigger> reference = new StateReference<>();
        reference.setState(initialState);
        stateAccessor = new Func<IState>() {

            @Implement
            public IState call() {
                return reference.getState();
            }
        };
        stateMutator = new Action1<IState>() {

            @Implement
            public void doIt(IState s) {
                reference.setState(s);
            }
        };
    }

    /**
     * Construct a state machine with external state storage.
     *
     * @param initialState The initial state
     * @param stateAccessor State accessor
     * @param stateMutator State mutator
     */
    public StateMachine(IState initialState, Func<IState> stateAccessor, Action1<IState> stateMutator, StateMachineConfig<IState, ITrigger> config) {
        this.config = config;
        this.stateAccessor = stateAccessor;
        this.stateMutator = stateMutator;
        stateMutator.doIt(initialState);
    }

    /**
     * The current state
     *
     * @return The current state
     */
    public IState getState() {
        return stateAccessor.call();
    }

    private void setState(IState value) {
        stateMutator.doIt(value);
    }

    /**
     * The currently-permissible trigger values
     *
     * @return The currently-permissible trigger values
     */
    public List<ITrigger> getPermittedTriggers() {
        return getCurrentRepresentation().getPermittedTriggers();
    }

    StateRepresentation<IState, ITrigger> getCurrentRepresentation() {
        StateRepresentation<IState, ITrigger> representation = config.getRepresentation(getState());
        return representation == null ? new StateRepresentation<IState, ITrigger>(getState()) : representation;
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked
     *
     * @param trigger The trigger to fire
     */
    public void fire(ITrigger trigger) {
        publicFire(trigger);
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked.
     *
     * @param trigger The trigger to fire
     * @param arg0    The first argument
     * @param <TArg0> Type of the first trigger argument
     */
    public <TArg0> void fire(TriggerWithParameters1<TArg0, IState, ITrigger> trigger, TArg0 arg0) {
        assert trigger != null : "trigger is null";
        publicFire(trigger.getTrigger(), arg0);
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked.
     *
     * @param trigger The trigger to fire
     * @param arg0    The first argument
     * @param arg1    The second argument
     * @param <TArg0> Type of the first trigger argument
     * @param <TArg1> Type of the second trigger argument
     */
    public <TArg0, TArg1> void fire(TriggerWithParameters2<TArg0, TArg1, IState, ITrigger> trigger, TArg0 arg0, TArg1 arg1) {
        assert trigger != null : "trigger is null";
        publicFire(trigger.getTrigger(), arg0, arg1);
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked.
     *
     * @param trigger The trigger to fire
     * @param arg0    The first argument
     * @param arg1    The second argument
     * @param arg2    The third argument
     * @param <TArg0> Type of the first trigger argument
     * @param <TArg1> Type of the second trigger argument
     * @param <TArg2> Type of the third trigger argument
     */
    public <TArg0, TArg1, TArg2> void fire(TriggerWithParameters3<TArg0, TArg1, TArg2, IState, ITrigger> trigger, TArg0 arg0, TArg1 arg1, TArg2 arg2) {
        assert trigger != null : "trigger is null";
        publicFire(trigger.getTrigger(), arg0, arg1, arg2);
    }

    protected void publicFire(ITrigger trigger, Object... args) {
        logger.info("Firing " + trigger);
        TriggerWithParameters<IState, ITrigger> configuration = config.getTriggerConfiguration(trigger);
        if (configuration != null) {
            configuration.validateParameters(args);
        }

        TriggerBehaviour<IState, ITrigger> triggerBehaviour = getCurrentRepresentation().tryFindHandler(trigger);
        if (triggerBehaviour == null) {
            unhandledTriggerAction.doIt(getCurrentRepresentation().getUnderlyingState(), trigger);
            return;
        }

        IState source = getState();
        OutVar<IState> destination = new OutVar<>();
        if (triggerBehaviour.resultsInTransitionFrom(source, args, destination)) {
            Transition<IState, ITrigger> transition = new Transition<>(source, destination.get(), trigger);

            getCurrentRepresentation().exit(transition);
            setState(destination.get());
            getCurrentRepresentation().enter(transition, args);
        }
    }

    /**
     * Override the default behaviour of throwing an exception when an unhandled trigger is fired
     *
     * @param unhandledTriggerAction An action to call when an unhandled trigger is fired
     */
    public void onUnhandledTrigger(Action2<IState, ITrigger> unhandledTriggerAction) {
        if (unhandledTriggerAction == null) {
            throw new IllegalStateException("unhandledTriggerAction");
        }
        this.unhandledTriggerAction = unhandledTriggerAction;
    }

    /**
     * Determine if the state machine is in the supplied state
     *
     * @param state The state to test for
     * @return True if the current state is equal to, or a substate of, the supplied state
     */
    public boolean isInState(IState state) {
        return getCurrentRepresentation().isIncludedIn(state);
    }

    /**
     * Returns true if {@code trigger} can be fired  in the current state
     *
     * @param trigger Trigger to test
     * @return True if the trigger can be fired, false otherwise
     */
    public boolean canFire(ITrigger trigger) {
        return getCurrentRepresentation().canHandle(trigger);
    }

    /**
     * A human-readable representation of the state machine
     *
     * @return A description of the current state and permitted triggers
     */
    @Override
    public String toString() {
        List<ITrigger> permittedTriggers = getPermittedTriggers();
        List<String> parameters = new ArrayList<>();

        for (ITrigger tTrigger : permittedTriggers) {
            parameters.add(tTrigger.toString());
        }

        StringBuilder params = new StringBuilder();
        String delim = "";
        for (String param : parameters) {
            params.append(delim);
            params.append(param);
            delim = ", ";
        }

        return String.format(
                "StateMachine {{ State = %s, PermittedTriggers = {{ %s }}}}",
                getState(),
                params.toString());
    }
}