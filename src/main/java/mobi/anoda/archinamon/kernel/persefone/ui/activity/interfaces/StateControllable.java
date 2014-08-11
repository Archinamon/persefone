package mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces;

import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
* @author: Archinamon
* @project: FavorMe
*/
public interface StateControllable {

    public static enum FragmentState {

        ATTACHED,
        DETACHED,
        INIT
    }

    FragmentState getState();

    /**
     * Calling on {@link AbstractFragment} connected to current {@link AbstractActivity}
     * that parametrized with instance of @this interface
     */
    void onRemove();

    /**
     * Calling on {@link AbstractFragment} removing from current {@link AbstractActivity}
     */
    void onSwitch();
}
