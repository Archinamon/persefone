package mobi.anoda.archinamon.kernel.persefone;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.async.AbstractAsyncTask;
import mobi.anoda.archinamon.kernel.persefone.network.client.ExtAndroidHttpClient;
import mobi.anoda.archinamon.kernel.persefone.receiver.InternetAccessReceiver;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.NetworkState;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AnodaApplicationDelegate extends Application {

    public static final  boolean         DEBUG           = true;
    private static final BroadcastFilter DEFAULT_FILTERS = new BroadcastFilter();
    private static volatile ContextWrapper       sAppContextProxy;
    protected               AbstractActivity     mContext;
    protected               ExtAndroidHttpClient mHttpClient;

    static {
        DEFAULT_FILTERS.addAction(NetworkNotification.FORCE_LOGOUT);
        DEFAULT_FILTERS.addAction(NetworkNotification.ALERT_EXCEPTION);
        DEFAULT_FILTERS.addAction(NetworkNotification.ALERT_NO_INTERNET);
        DEFAULT_FILTERS.addAction(NetworkNotification.INTERNET_ACCESS_GRANTED);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContextProxy = this;

        AbstractAsyncTask.init();
        State.svAccessState = InternetAccessReceiver.checkConnection(this) ? State.ACCESS_GRANTED : State.ACCESS_DENIED;
    }

    /**
     * @return an Application as ContextWrapper proxy instance
     * @hide
     */
    public static Context getProxyContext() {
        return AnodaApplicationDelegate.sAppContextProxy;
    }

    public final BroadcastFilter getDefaultNetworkEvents() {
        return DEFAULT_FILTERS;
    }

    protected final void setDefaultScreen(String who, Class<? extends AbstractActivity> screen) {
        try {
            NetworkState.setDefaultOnForceLogoutScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    protected final void setServerOfflineScreen(String who, Class<? extends AbstractDialog> screen) {
        try {
            NetworkState.setDefaultOnServerOfflineScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    public abstract ExtAndroidHttpClient getHttpClient();
}
