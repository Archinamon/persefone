package mobi.anoda.archinamon.kernel.persefone.stateless;

public class StateReference<IState, ITrigger> {

    private IState state;

    public IState getState() {
        return state;
    }

    public void setState(IState value) {
        state = value;
    }
}