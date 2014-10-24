package mobi.anoda.archinamon.kernel.persefone.stateless.triggers;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.stateless.OutVar;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.FuncBoolean;

public class IgnoredTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {

    public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
        super(trigger, guard);
    }

    @Implement
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        return false;
    }
}