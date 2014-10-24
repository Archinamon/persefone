package mobi.anoda.archinamon.kernel.persefone.stateless.transitions;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.stateless.OutVar;
import mobi.anoda.archinamon.kernel.persefone.stateless.delegates.FuncBoolean;
import mobi.anoda.archinamon.kernel.persefone.stateless.triggers.TriggerBehaviour;

public class TransitioningTriggerBehaviour<IState, ITrigger> extends TriggerBehaviour<IState, ITrigger> {

    private final IState destination;

    public TransitioningTriggerBehaviour(ITrigger trigger, IState destination, FuncBoolean guard) {
        super(trigger, guard);
        this.destination = destination;
    }

    @Implement
    public boolean resultsInTransitionFrom(IState source, Object[] args, OutVar<IState> dest) {
        dest.set(destination);
        return true;
    }
}