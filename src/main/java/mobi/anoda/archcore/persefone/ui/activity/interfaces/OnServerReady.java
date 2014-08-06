package mobi.anoda.archcore.persefone.ui.activity.interfaces;

import mobi.anoda.archcore.persefone.services.AbstractAsyncServer;
import mobi.anoda.archcore.persefone.services.AsyncRequest;
import mobi.anoda.archcore.persefone.ui.fragment.AbstractFragment;

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
