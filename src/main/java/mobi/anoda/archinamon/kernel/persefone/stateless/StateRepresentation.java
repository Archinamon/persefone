package mobi.anoda.archinamon.kernel.persefone.stateless;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Action1;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Action2;
import mobi.anoda.archinamon.kernel.persefone.stateless.transitions.Transition;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerBehaviour;

public class StateRepresentation<IState, ITrigger> {

    private final IState state;

    private final Map<ITrigger, List<TriggerBehaviour<IState, ITrigger>>> triggerBehaviours = new HashMap<>();
    private final List<Action2<Transition<IState, ITrigger>, Object[]>>   entryActions      = new ArrayList<>();
    private final List<Action1<Transition<IState, ITrigger>>>             exitActions       = new ArrayList<>();
    private final List<StateRepresentation<IState, ITrigger>>             substates         = new ArrayList<>();
    private StateRepresentation<IState, ITrigger> superstate; // null

    public StateRepresentation(IState state) {
        this.state = state;
    }

    protected Map<ITrigger, List<TriggerBehaviour<IState, ITrigger>>> getTriggerBehaviours() {
        return triggerBehaviours;
    }

    public Boolean canHandle(ITrigger trigger) {
        return tryFindHandler(trigger) != null;
    }

    public TriggerBehaviour<IState, ITrigger> tryFindHandler(ITrigger trigger) {
        TriggerBehaviour result = tryFindLocalHandler(trigger);
        if (result == null && superstate != null) {
            result = superstate.tryFindHandler(trigger);
        }
        return result;
    }

    TriggerBehaviour<IState, ITrigger> tryFindLocalHandler(ITrigger trigger/*, out TriggerBehaviour handler*/) {
        List<TriggerBehaviour<IState, ITrigger>> possible = triggerBehaviours.get(trigger);
        if (possible == null) {
            return null;
        }

        List<TriggerBehaviour<IState, ITrigger>> actual = new ArrayList<>();
        for (TriggerBehaviour<IState, ITrigger> triggerBehaviour : possible) {
            if (triggerBehaviour.isGuardConditionMet()) {
                actual.add(triggerBehaviour);
            }
        }

        if (actual.size() > 1) {
            throw new IllegalStateException("Multiple permitted exit transitions are configured from state '" + trigger + "' for trigger '" + state + "'. Guard clauses must be mutually exclusive.");
        }

        return actual.isEmpty() ? null : actual.get(0);
    }

    public void addEntryAction(final ITrigger trigger, final Action2<Transition<IState, ITrigger>, Object[]> action) {
        assert action != null : "action is null";

        entryActions.add(new Action2<Transition<IState, ITrigger>, Object[]>() {
            @Override
            public void doIt(Transition<IState, ITrigger> t, Object[] args) {
                if (t.getTrigger().equals(trigger)) {
                    action.doIt(t, args);
                }
            }
        });
    }

    public void addEntryAction(Action2<Transition<IState, ITrigger>, Object[]> action) {
        assert action != null : "action is null";
        entryActions.add(action);
    }

    public void insertEntryAction(Action2<Transition<IState, ITrigger>, Object[]> action) {
        assert action != null : "action is null";
        entryActions.add(0, action);
    }

    public void addExitAction(Action1<Transition<IState, ITrigger>> action) {
        assert action != null : "action is null";
        exitActions.add(action);
    }

    public void enter(Transition<IState, ITrigger> transition, Object... entryArgs) {
        assert transition != null : "transition is null";

        if (transition.isReentry()) {
            executeEntryActions(transition, entryArgs);
        } else if (!includes(transition.getSource())) {
            if (superstate != null) {
                superstate.enter(transition, entryArgs);
            }

            executeEntryActions(transition, entryArgs);
        }
    }

    public void exit(Transition<IState, ITrigger> transition) {
        assert transition != null : "transition is null";

        if (transition.isReentry()) {
            executeExitActions(transition);
        } else if (!includes(transition.getDestination())) {
            executeExitActions(transition);
            if (superstate != null) {
                superstate.exit(transition);
            }
        }
    }

    void executeEntryActions(Transition<IState, ITrigger> transition, Object[] entryArgs) {
        assert transition != null : "transition is null";
        assert entryArgs != null : "entryArgs is null";
        for (Action2<Transition<IState, ITrigger>, Object[]> action : entryActions) {
            action.doIt(transition, entryArgs);
        }
    }

    void executeExitActions(Transition<IState, ITrigger> transition) {
        assert transition != null : "transition is null";
        for (Action1<Transition<IState, ITrigger>> action : exitActions) {
            action.doIt(transition);
        }
    }

    public void addTriggerBehaviour(TriggerBehaviour<IState, ITrigger> triggerBehaviour) {
        List<TriggerBehaviour<IState, ITrigger>> allowed;
        if (!triggerBehaviours.containsKey(triggerBehaviour.getTrigger())) {
            allowed = new ArrayList<>();
            triggerBehaviours.put(triggerBehaviour.getTrigger(), allowed);
        }
        allowed = triggerBehaviours.get(triggerBehaviour.getTrigger());
        allowed.add(triggerBehaviour);
    }

    public StateRepresentation<IState, ITrigger> getSuperstate() {
        return superstate;
    }

    public void setSuperstate(StateRepresentation<IState, ITrigger> value) {
        superstate = value;
    }

    public IState getUnderlyingState() {
        return state;
    }

    public void addSubstate(StateRepresentation<IState, ITrigger> substate) {
        assert substate != null : "substate is null";
        substates.add(substate);
    }

    public boolean includes(IState stateToCheck) {
        for (StateRepresentation<IState, ITrigger> s : substates) {
            if (s.includes(stateToCheck)) {
                return true;
            }
        }
        return this.state.equals(stateToCheck);
    }

    public boolean isIncludedIn(IState stateToCheck) {
        return this.state.equals(stateToCheck) || (superstate != null && superstate.isIncludedIn(stateToCheck));
    }

    @SuppressWarnings("unchecked")
    public List<ITrigger> getPermittedTriggers() {
        Set<ITrigger> result = new HashSet<>();

        for (ITrigger t : triggerBehaviours.keySet()) {
            for (TriggerBehaviour<IState, ITrigger> v : triggerBehaviours.get(t)) {
                if (v.isGuardConditionMet()) {
                    result.add(t);
                    break;
                }
            }
        }

        if (getSuperstate() != null) {
            result.addAll(getSuperstate().getPermittedTriggers());
        }

        return new ArrayList<>(result);
    }
}