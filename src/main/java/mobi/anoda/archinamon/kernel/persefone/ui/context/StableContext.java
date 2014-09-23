package mobi.anoda.archinamon.kernel.persefone.ui.context;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.Window;
import org.intellij.lang.annotations.MagicConstant;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Permission;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.utils.Singleton;

/**
 * Created by matsukov-ea on 23.09.2014.
 */
public interface StableContext {

    public static final class Impl {

        private static final Singleton<ProxyContextWrapper> gDefault = new Singleton<ProxyContextWrapper>() {

            @Implement
            protected ProxyContextWrapper create() {
                ProxyContextWrapper impl = new ProxyContextWrapper() {};
                impl.construct();
                return impl;
            }
        };

        private Impl() {
        }

        public static <I extends Context> StableContext instantiate(I contextImpl) {
            ProxyContextWrapper impl = gDefault.getInstance();
            impl.update(impl, contextImpl);

            return impl;
        }

        public static StableContext obtain() {
            return gDefault.getInstance();
        }
    }

    boolean isUiContextRegistered();

    boolean isRiContextRegistered();

    boolean isNsContextRegistered();

    <Impl extends AbstractActivity> Impl obtainUiContext();

    <Impl extends AbstractService> Impl obtainRiContext();

    <Impl extends Context> Impl obtainNsContext();

    <Impl extends AnodaApplicationDelegate> Impl obtainAppContext();

    View getView();

    ComponentName getInfo();

    void shoutToast(String msg);

    View findViewById(@IdRes int id);

    @ProxyMethod
    String getPackageName();

    @ProxyMethod
    String getString(@StringRes int id, Object... varargs);

    @ProxyMethod
    Window getWindow();

    @ProxyMethod
    void startActivity(Intent data);

    @ProxyMethod
    Object getSystemService(@MagicConstant(valuesFromClass = Context.class) String name);

    @ProxyMethod
    boolean bindService(@NonNull Intent callArgs, ServiceConnection callback, @MagicConstant(flagsFromClass = Context.class) int mode);

    @ProxyMethod
    void unbindService(ServiceConnection callback);

    @ProxyMethod
    void registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

    @ProxyMethod
    void registerReceiver(BroadcastReceiver receiver, IntentFilter filter, Permission permission);

    @ProxyMethod
    void unregisterReceiver(BroadcastReceiver receiver);

    @ProxyMethod
    void startService(Intent intent);

    @ProxyMethod
    void sendBroadcast(Intent intent);

    @ProxyMethod
    void sendBroadcast(Intent intent, Permission receiverPermission);

    @ProxyMethod
    View getCurrentFocus();
}
