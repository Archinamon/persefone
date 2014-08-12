package mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces;

import mobi.anoda.archinamon.kernel.persefone.service.async.AbstractAsyncServer;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
* @author: Archinamon
* @project: FavorMe
*/
public interface OnServerReady {

    /**
     * Called when {@link Fragment} instance connected to the AsyncServer implementation
     */
    void onBind();

    /**
     * Called every time when {@link AbstractFragment} release an {@link AsyncRequest}
     */
    void onRendezvous(AsyncRequest request);

    /**
     * Called on {@link AbstractAsyncServer} disconnecting from current activity
     */
    void onDisconnect();
}
