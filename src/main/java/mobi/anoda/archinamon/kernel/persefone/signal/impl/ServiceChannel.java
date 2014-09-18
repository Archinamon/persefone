package mobi.anoda.archinamon.kernel.persefone.signal.impl;

import android.content.IntentFilter;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public enum ServiceChannel implements Broadcastable {

    CALL_SOCIAL_API(".CALL_SOCIAL_API"),
    CALL_REST_API(".CALL_REST_API");

    public static final  String KEY_DATA = ".key:data";
    private static final String BASE     = "mobi.anoda.archcore.persefone.services.channel";
    private String mChannelTarget;

    private ServiceChannel(String name) {
        mChannelTarget = BASE + name;
    }

    @Implement
    public String getAction() {
        return mChannelTarget;
    }

    @Implement
    public boolean isEqual(String with) {
        return this.mChannelTarget.equalsIgnoreCase(with);
    }

    public IntentFilter getChannel() {
        return new IntentFilter(mChannelTarget);
    }
}
