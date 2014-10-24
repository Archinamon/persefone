package mobi.anoda.archinamon.kernel.persefone.stateless.triggers;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.stateless.OutVar;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.Func2;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.FuncBoolean;

public class DynamicTriggerBehaviour<IState, ITrigger> extends TriggerBehaviour<IState, ITrigger> {

    private final Func2<Object[], IState> destination;

    public DynamicTriggerBehaviour(ITrigger trigger, Func2<Object[], IState> destination, FuncBoolean guard) {
        super(trigger, guard);
        assert destination != null : "destination is null";
        this.destination = destination;
    }

    @Implement
    public boolean resultsInTransitionFrom(IState source, Object[] args, OutVar<IState> dest) {
        dest.set(destination.call(args));
        return true;
    }
}