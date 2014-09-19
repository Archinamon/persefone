package mobi.anoda.archinamon.kernel.persefone.ui.context;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import org.intellij.lang.annotations.MagicConstant;
import java.util.Observable;
import java.util.Observer;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Permission;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.utils.ActivityUtils;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
abstract class AbstractStableContext extends Observable implements Observer {

    private volatile AbstractActivity         mUiContext; //user interface context;
    private volatile AbstractService          mRiContext; //remote interface context;
    private volatile Context                  mNsContext; //neighbour context, e.g. BroadcastReceiver
    private          AnodaApplicationDelegate mAppContext;//application reference, always nonnull if any app task exists

    protected final void construct() throws ClassCastException {
        Application androidAppContext = ActivityUtils.getApplicationContext();
        if (androidAppContext instanceof AnodaApplicationDelegate)
            this.mAppContext = (AnodaApplicationDelegate) androidAppContext;
        else throw new ClassCastException("Should be defined AnodaApplicationDelegate extended implementation");
    }

    @Implement
    public final void update(Observable observable, Object data) {
        if (data instanceof AbstractActivity) {
            mUiContext = (AbstractActivity) data;
        } else if (data instanceof AbstractService) {
            mRiContext = (AbstractService) data;
        } else if (data instanceof Context) {
            mNsContext = (Context) data;
        } else {
            throw new IllegalArgumentException("param 'data' should extends Context class");
        }
    }

    public final boolean isUiContextRegistered() {
        return this.mUiContext != null;
    }

    public final boolean isRiContextRegistered() {
        return this.mRiContext != null;
    }

    public final boolean isNsContextRegistered() {
        return this.mNsContext != null;
    }

    @SuppressWarnings("unchecked")
    public <Impl extends AbstractActivity> Impl obtainUiContext() {
        return (Impl) this.mUiContext;
    }

    @SuppressWarnings("unchecked")
    public <Impl extends AbstractService> Impl obtainRiContext() {
        return (Impl) this.mRiContext;
    }

    @SuppressWarnings("unchecked")
    public <Impl extends Context> Impl obtainNsContext() {
        return (Impl) this.mNsContext;
    }

    @SuppressWarnings("unchecked")
    public <Impl extends AnodaApplicationDelegate> Impl obtainAppContext() {
        return (Impl) this.mAppContext;
    }

    @Nullable
    public View getView() {
        return findViewById(android.R.id.content);
    }

    @Nullable
    @ProxyMethod
    public Window getWindow() {
        if (isUiContextRegistered()) return mUiContext.getWindow();

        return null;
    }

    @Nullable
    @ProxyMethod
    public View findViewById(@IdRes int id) {
        if (isUiContextRegistered()) return mUiContext.findViewById(id);

        return null;
    }

    @ProxyMethod
    public Object getSystemService(@MagicConstant(valuesFromClass = Context.class) String name) {
        return obtainStable().getSystemService(name);
    }

    @ProxyMethod
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        obtainStable().registerReceiver(receiver, filter);
    }

    @ProxyMethod
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter, Permission permission) {
        obtainStable().registerReceiver(receiver, filter, permission.getPermission(), null);
    }

    @ProxyMethod
    public void unregisterReceiver(BroadcastReceiver receiver) {
        obtainStable().unregisterReceiver(receiver);
    }

    @ProxyMethod
    public void startService(Intent intent) {
        obtainStable().startService(intent);
    }

    @ProxyMethod
    public void sendBroadcast(Intent intent) {
        obtainStable().sendBroadcast(intent);
    }

    @ProxyMethod
    public void sendBroadcast(Intent intent, Permission receiverPermission) {
        obtainStable().sendBroadcast(intent, receiverPermission.getPermission());
    }

    @ProxyMethod
    public View getCurrentFocus() {
        if (isUiContextRegistered()) return mUiContext.getCurrentFocus();
        return null;
    }

    private Context obtainStable() {
        if (mUiContext != null) return mUiContext; //ui has the highest priority to reference above
        if (mRiContext != null) return mRiContext;
        if (mNsContext != null) return mNsContext;
        return mAppContext;
    }
}
