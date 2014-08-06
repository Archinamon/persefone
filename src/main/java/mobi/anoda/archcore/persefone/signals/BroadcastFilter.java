package mobi.anoda.archcore.persefone.signals;

import android.content.IntentFilter;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class BroadcastFilter extends IntentFilter {

    public BroadcastFilter() {
        super();
    }

    public BroadcastFilter(Broadcastable command) {
        super(command.getAction());
    }

    public void addAction(Broadcastable command) {
        addAction(command.getAction());
    }
}
