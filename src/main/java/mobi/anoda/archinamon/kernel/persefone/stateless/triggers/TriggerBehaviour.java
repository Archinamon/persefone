package mobi.anoda.archinamon.kernel.persefone.stateless.triggers;

import mobi.anoda.archinamon.kernel.persefone.stateless.OutVar;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.FuncBoolean;

public abstract class TriggerBehaviour<IState, ITrigger> {

    private final ITrigger    trigger;
    private final FuncBoolean guard;

    protected TriggerBehaviour(ITrigger trigger, FuncBoolean guard) {
        this.trigger = trigger;
        this.guard = guard;
    }

    public ITrigger getTrigger() {
        return trigger;
    }

    public boolean isGuardConditionMet() {
        return guard.call();
    }

    public abstract boolean resultsInTransitionFrom(IState source, Object[] args, OutVar<IState> dest);
}