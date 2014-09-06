package mobi.anoda.archinamon.kernel.persefone;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.async.AbstractAsyncTask;
import mobi.anoda.archinamon.kernel.persefone.network.client.ExtAndroidHttpClient;
import mobi.anoda.archinamon.kernel.persefone.receiver.InternetAccessReceiver;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AnodaApplicationDelegate extends Application {

    public static final boolean DEBUG = true;
    private static          String               sFlurryKey;
    private static volatile ContextWrapper       sAppContextProxy;
    protected               AbstractActivity     mContext;
    protected               ExtAndroidHttpClient mHttpClient;

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
    public final static Context getProxyContext() {
        return AnodaApplicationDelegate.sAppContextProxy;
    }

    protected final void setDefaultScreen(String who, Class<? extends AbstractActivity> screen) {
        try {
            AbstractActivity.setDefaultOnForceLogoutScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    protected final void setServerOfflineScreen(String who, Class<? extends AbstractDialog> screen) {
        try {
            AbstractActivity.setDefaultOnServerOfflineScreen(screen);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LogHelper.println_error(who, e);
        }
    }

    public final void setFlurryKey(String key) {
        sFlurryKey = key;
    }

    public final String getFlurryKey() {
        return sFlurryKey;
    }

    public abstract void registerContext(AbstractActivity context);

    public abstract void unregisterContext();

    public abstract AbstractActivity getContext();

    public abstract ExtAndroidHttpClient getHttpClient();
}
