package mobi.anoda.archcore.persefone;

import android.app.Application;
import mobi.anoda.archcore.persefone.network.ExtAndroidHttpClient;
import mobi.anoda.archcore.persefone.network.State;
import mobi.anoda.archcore.persefone.receivers.InternetAccessReceiver;
import mobi.anoda.archcore.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archcore.persefone.ui.dialog.AbstractPopup;
import mobi.anoda.archcore.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AnodaApplicationDelegate extends Application {

    public static final  boolean DEBUG                = true;
    private static String               mFlurryKey;
    protected      AbstractActivity     mContext;
    protected      ExtAndroidHttpClient mHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        State.svAccessState = InternetAccessReceiver.checkConnection(this)
                              ? State.ACCESS_GRANTED
                              : State.ACCESS_DENIED;
    }

    protected final void setDefaultScreen(String who, Class<? extends AbstractActivity> screen) {
        try {
            AbstractActivity.setDefaultOnForceLogoutScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    protected final void setServerOfflineScreen(String who, Class<? extends AbstractPopup> screen) {
        try {
            AbstractActivity.setDefaultOnServerOfflineScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    protected final void setFlurryKey(String key) {
        mFlurryKey = key;
    }

    public final String getFlurryKey() {
        return mFlurryKey;
    }

    public abstract void registerContext(AbstractActivity context);

    public abstract void unregisterContext();

    public abstract AbstractActivity getContext();

    public abstract ExtAndroidHttpClient getHttpClient();
}
