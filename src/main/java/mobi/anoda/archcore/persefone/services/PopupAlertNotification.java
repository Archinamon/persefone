package mobi.anoda.archcore.persefone.services;

import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.signals.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public enum PopupAlertNotification implements Broadcastable {

    PASS(".PASS");

    public static final  String KEY_DATA = ".key:data";
    private static final String BASE     = "mobi.anoda.archcore.persefone.services.versions";
    private String mVersions;

    private PopupAlertNotification(String name) {
        mVersions = BASE + name;
    }

    @Implement
    public String getAction() {
        return mVersions;
    }

    @Implement
    public boolean isEqual(String with) {
        return mVersions.equalsIgnoreCase(with);
    }
}
