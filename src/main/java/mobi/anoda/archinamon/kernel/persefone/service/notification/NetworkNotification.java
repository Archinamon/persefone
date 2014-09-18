package mobi.anoda.archinamon.kernel.persefone.service.notification;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public enum NetworkNotification implements Broadcastable {

    FORCE_LOGOUT(".FORCE_LOGOUT"),
    ALERT_EXCEPTION(".ALERT_EXCEPTION"),
    ALERT_NO_INTERNET(".ALERT_NO_INTERNET"),
    INTERNET_ACCESS_DENIED(".internet_state.DENIED"),
    INTERNET_ACCESS_GRANTED(".internet_state.GRANTED"),
    NOTIFY_DISMISS(".NOTIFY_DISMISS");

    public static final  String KEY_DATA = ".key:data";
    private static final String BASE     = "mobi.anoda.archcore.persefone.services.notification";
    private String mNotification;

    private NetworkNotification(String name) {
        mNotification = BASE + name;
    }

    @Implement
    public String getAction() {
        return mNotification;
    }

    @Implement
    public boolean isEqual(String with) {
        return mNotification.equalsIgnoreCase(with);
    }
}
