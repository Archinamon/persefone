package mobi.anoda.archinamon.kernel.persefone.stateless;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.*;
import mobi.anoda.archinamon.kernel.persefone.stateless.transitions.*;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.*;

public class StateConfiguration<IState, ITrigger> {

    private static final FuncBoolean NO_GUARD = new FuncBoolean() {

        @Implement
        public boolean call() {
            return true;
        }
    };
    private final StateRepresentation<IState, ITrigger>                representation;
    private final Func2<IState, StateRepresentation<IState, ITrigger>> lookup;

    public StateConfiguration(final StateRepresentation<IState, ITrigger> representation, final Func2<IState, StateRepresentation<IState, ITrigger>> lookup) {
        assert representation != null : "representation is null";
        assert lookup != null : "lookup is null";
        this.representation = representation;
        this.lookup = lookup;
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permit(ITrigger trigger, IState destinationState) {
        enforceNotIdentityTransition(destinationState);
        return publicPermit(trigger, destinationState);
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param guard            Function that must return true in order for the trigger to be accepted
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permitIf(ITrigger trigger, IState destinationState, FuncBoolean guard) {
        enforceNotIdentityTransition(destinationState);
        return publicPermitIf(trigger, destinationState, guard);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the configured state transitions to an identical sibling state
     * <p/>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permitReentry(ITrigger trigger) {
        return publicPermit(trigger, representation.getUnderlyingState());
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the configured state transitions to an identical sibling state
     * <p/>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permitReentryIf(ITrigger trigger, FuncBoolean guard) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard);
    }

    /**
     * ignore the specified trigger when in the configured state
     *
     * @param trigger The trigger to ignore
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> ignore(ITrigger trigger) {
        return ignoreIf(trigger, NO_GUARD);
    }

    /**
     * ignore the specified trigger when in the configured state, if the guard returns true
     *
     * @param trigger The trigger to ignore
     * @param guard   Function that must return true in order for the trigger to be ignored
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> ignoreIf(ITrigger trigger, FuncBoolean guard) {
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(new IgnoredTriggerBehaviour<IState, ITrigger>(trigger, guard));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onEntry(final Action entryAction) {
        assert entryAction != null : "entryAction is null";
        return onEntry(new Action1<Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(Transition<IState, ITrigger> t) {
                entryAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onEntry(final Action1<Transition<IState, ITrigger>> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(new Action2<Transition<IState, ITrigger>, Object[]>() {

            @Implement
            public void doIt(Transition<IState, ITrigger> arg1, Object[] arg2) {
                entryAction.doIt(arg1);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onEntryFrom(ITrigger trigger, final Action entryAction) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action1<Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(Transition<IState, ITrigger> arg1) {
                entryAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onEntryFrom(ITrigger trigger, final Action1<Transition<IState, ITrigger>> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger, new Action2<Transition<IState, ITrigger>, Object[]>() {

            @Implement
            public void doIt(Transition<IState, ITrigger> arg1, Object[] arg2) {
                entryAction.doIt(arg1);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     *
     * @return The receiver
     */
    public <TArg0> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters1<TArg0, IState, ITrigger> trigger, final Action1<TArg0> entryAction, final Class<TArg0> classe0) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action2<TArg0, Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(TArg0 arg1, Transition<IState, ITrigger> arg2) {
                entryAction.doIt(arg1);
            }
        }, classe0);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     *
     * @return The receiver
     */
    public <TArg0> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters1<TArg0, IState, ITrigger> trigger, final Action2<TArg0, Transition<IState, ITrigger>> entryAction, final Class<TArg0> classe0) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<IState, ITrigger>, Object[]>() {

            @SuppressWarnings("unchecked")
            @Implement
            public void doIt(Transition<IState, ITrigger> t, Object[] arg2) {
                entryAction.doIt((TArg0) arg2[0], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters2<TArg0, TArg1, IState, ITrigger> trigger, final Action2<TArg0, TArg1> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action3<TArg0, TArg1, Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(TArg0 a0, TArg1 a1, Transition<IState, ITrigger> t) {
                entryAction.doIt(a0, a1);
            }
        }, classe0, classe1);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters2<TArg0, TArg1, IState, ITrigger> trigger, final Action3<TArg0, TArg1, Transition<IState, ITrigger>> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<IState, ITrigger>, Object[]>() {

            @SuppressWarnings("unchecked")
            @Implement
            public void doIt(Transition<IState, ITrigger> t, Object[] args) {
                entryAction.doIt((TArg0) args[0], (TArg1) args[1], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters3<TArg0, TArg1, TArg2, IState, ITrigger> trigger, final Action3<TArg0, TArg1, TArg2> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1, final Class<TArg2> classe2) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action4<TArg0, TArg1, TArg2, Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(TArg0 a0, TArg1 a1, TArg2 a2, Transition<IState, ITrigger> t) {
                entryAction.doIt(a0, a1, a2);
            }
        }, classe0, classe1, classe2);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<IState, ITrigger> onEntryFrom(TriggerWithParameters3<TArg0, TArg1, TArg2, IState, ITrigger> trigger,
                                                                                  final Action4<TArg0, TArg1, TArg2, Transition<IState, ITrigger>> entryAction,
                                                                                  final Class<TArg0> classe0,
                                                                                  final Class<TArg1> classe1,
                                                                                  final Class<TArg2> classe2) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<IState, ITrigger>, Object[]>() {

            @SuppressWarnings("unchecked")
            @Implement
            public void doIt(Transition<IState, ITrigger> t, Object[] args) {
                entryAction.doIt((TArg0) args[0], (TArg1) args[1], (TArg2) args[2], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onExit(final Action exitAction) {
        assert exitAction != null : "exitAction is null";
        return onExit(new Action1<Transition<IState, ITrigger>>() {

            @Implement
            public void doIt(Transition<IState, ITrigger> arg1) {
                exitAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> onExit(Action1<Transition<IState, ITrigger>> exitAction) {
        assert exitAction != null : "exitAction is null";
        representation.addExitAction(exitAction);
        return this;
    }

    /**
     * Sets the superstate that the configured state is a substate of
     * <p/>
     * Substates inherit the allowed transitions of their superstate. When entering directly into a substate from outside of the superstate, entry actions for the superstate are executed. Likewise when leaving from the substate to outside the supserstate, exit actions for the superstate will
     * execute.
     *
     * @param superstate The superstate
     *
     * @return The receiver
     */
    public StateConfiguration<IState, ITrigger> substateOf(IState superstate) {
        StateRepresentation<IState, ITrigger> superRepresentation = lookup.call(superstate);
        representation.setSuperstate(superRepresentation);
        superRepresentation.addSubstate(representation);
        return this;
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permitDynamic(ITrigger trigger, final Func<IState> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     *
     * @return The receiver
     */
    public <TArg0> StateConfiguration<IState, ITrigger> permitDynamic(TriggerWithParameters1<TArg0, IState, ITrigger> trigger, Func2<TArg0, IState> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<IState, ITrigger> permitDynamic(TriggerWithParameters2<TArg0, TArg1, IState, ITrigger> trigger, Func3<TArg0, TArg1, IState> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     *
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<IState, ITrigger> permitDynamic(TriggerWithParameters3<TArg0, TArg1, TArg2, IState, ITrigger> trigger, final Func4<TArg0, TArg1, TArg2, IState> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     *
     * @return The reciever
     */
    public StateConfiguration<IState, ITrigger> permitDynamicIf(ITrigger trigger, final Func<IState> destinationStateSelector, FuncBoolean guard) {
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger, new Func2<Object[], IState>() {

            @Implement
            public IState call(Object[] arg0) {
                return destinationStateSelector.call();
            }
        }, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     *
     * @return The reciever
     */
    public <TArg0> StateConfiguration<IState, ITrigger> permitDynamicIf(TriggerWithParameters1<TArg0, IState, ITrigger> trigger, final Func2<TArg0, IState> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger.getTrigger(), new Func2<Object[], IState>() {

            @SuppressWarnings("unchecked")
            @Implement
            public IState call(Object[] args) {
                return destinationStateSelector.call((TArg0) args[0]);

            }
        }, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     *
     * @return The reciever
     */
    public <TArg0, TArg1> StateConfiguration<IState, ITrigger> permitDynamicIf(TriggerWithParameters2<TArg0, TArg1, IState, ITrigger> trigger, final Func3<TArg0, TArg1, IState> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger.getTrigger(), new Func2<Object[], IState>() {

            @SuppressWarnings("unchecked")
            @Implement
            public IState call(Object[] args) {
                return destinationStateSelector.call((TArg0) args[0], (TArg1) args[1]);
            }
        }, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     *
     * @return The reciever
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<IState, ITrigger> permitDynamicIf(TriggerWithParameters3<TArg0, TArg1, TArg2, IState, ITrigger> trigger, final Func4<TArg0, TArg1, TArg2, IState> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger.getTrigger(), new Func2<Object[], IState>() {

            @SuppressWarnings("unchecked")
            @Implement
            public IState call(Object[] args) {
                return destinationStateSelector.call((TArg0) args[0], (TArg1) args[1], (TArg2) args[2]);
            }
        }, guard);
    }

    void enforceNotIdentityTransition(IState destination) {
        if (destination.equals(representation.getUnderlyingState())) {
            throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. To accept a trigger without changing state, use either Ignore() or PermitReentry().");
        }
    }

    StateConfiguration<IState, ITrigger> publicPermit(ITrigger trigger, IState destinationState) {
        return publicPermitIf(trigger, destinationState, NO_GUARD);
    }

    StateConfiguration<IState, ITrigger> publicPermitIf(ITrigger trigger, IState destinationState, FuncBoolean guard) {
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(new TransitioningTriggerBehaviour<>(trigger, destinationState, guard));
        return this;
    }

    StateConfiguration<IState, ITrigger> publicPermitDynamic(ITrigger trigger, Func2<Object[], IState> destinationStateSelector) {
        return publicPermitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    StateConfiguration<IState, ITrigger> publicPermitDynamicIf(ITrigger trigger, Func2<Object[], IState> destinationStateSelector, FuncBoolean guard) {
        assert destinationStateSelector != null : "destinationStateSelector is null";
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, destinationStateSelector, guard));
        return this;
    }
}