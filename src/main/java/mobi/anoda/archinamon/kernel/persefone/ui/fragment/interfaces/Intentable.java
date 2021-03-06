package mobi.anoda.archinamon.kernel.persefone.ui.fragment.interfaces;

import android.content.Intent;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
* @author: Archinamon
* @project: FavorMe
*/
public interface Intentable {

    /**
     * Should be called inside the {@link AbstractActivity:OnNewIntent} method
     * @param i {@link Intent} that should be delivered to arbitrary {@link AbstractFragment}
     */
    void onReceiveIntent(Intent i);
}
