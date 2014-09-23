package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import java.util.ArrayList;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.receiver.AbstractReceiver;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.AsyncReceiver;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Permission;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;

/**
 * Created by matsukov-ea on 19.09.2014.
 */
public final class BroadcastBus {

    public static final String CUSTOM_DATA = ".bus:key_data";
    private final StableContext mStableContext;
    private       AsyncReceiver mAsyncListener;
    private volatile boolean isExternalSignals;

    private final AbstractReceiver mMainAsyncReceiver = new AbstractReceiver() {

        @Implement
        public void onReceive(@NonNull final String action, @Nullable Intent data) {
            Exception any = null;
            boolean isDelivered = false;

            try {
                if (mAsyncListener != null) {
                    isDelivered = mAsyncListener.onReceive(action, data);
                }
            } catch (Exception ignore) {
                isDelivered = false;
                any = ignore;
            } finally {
                if (!isDelivered && mAsyncListener != null) {
                    mAsyncListener.onException(any);
                }
            }
        }
    };

    public BroadcastBus(StableContext stableContext) {
        this.mStableContext = stableContext;
        this.isExternalSignals = false;
    }

    public void setAsyncReceiver(AsyncReceiver receiver, boolean isExternal) {
        mAsyncListener = receiver;
        isExternalSignals = isExternal;
    }

    /* Registering and de-registering listeners and receivers from context */

    public void register(@NonNull BroadcastFilter actions) {
        mStableContext.registerReceiver(mMainAsyncReceiver, actions);
    }

    public void unregister() {
        mStableContext.unregisterReceiver(mMainAsyncReceiver);
    }

    public final void registerNetworkEventsForCurrentUiContext() {
        final NetworkState networkWatchdog = NetworkState.obtain(mStableContext);
        final AnodaApplicationDelegate appContext = mStableContext.obtainAppContext();
        mStableContext.registerReceiver(networkWatchdog.getNetworkErrorProcessor(), appContext.getDefaultNetworkEvents());
    }

    public final void unregisterNetworkEventsForCurrentUiContext() {
        final NetworkState networkWatchdog = NetworkState.obtain(mStableContext);
        mStableContext.unregisterReceiver(networkWatchdog.getNetworkErrorProcessor());
    }

    /* Synthetic broadcastable commands */

    public void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), service);
        intent.setAction(command.getAction());
        mStableContext.startService(intent);
    }

    public <T extends Parcelable> void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service, T data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), service);
        intent.setAction(command.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        mStableContext.startService(intent);
    }

    public <T extends Parcelable> void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service, ArrayList<T> data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), service);
        intent.setAction(command.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        mStableContext.startService(intent);
    }

    public void sendOrderedBroadcastWithCallback(Broadcastable command, Permission permission, Broadcastable callbackCommand) {
        Intent i = new Intent(command.getAction()).putExtra(CUSTOM_DATA, callbackCommand.getAction());
        mStableContext.sendBroadcast(i, permission);
    }

    /* universal broadcaster */
    public void sendBroadcast(Broadcastable command) {
        sendBroadcast(command, (Parcelable) null);
    }

    public void sendBroadcast(Broadcastable command, String data) {
        Bundle params = new Bundle();
        params.putString(CUSTOM_DATA, data);
        sendBroadcast(command, params);
    }

    public void sendBroadcast(Broadcastable command, int data) {
        Bundle params = new Bundle();
        params.putInt(CUSTOM_DATA, data);
        sendBroadcast(command, params);
    }

    public <Model extends Parcelable> void sendBroadcast(Broadcastable command, Model data) {
        Intent intent = new Intent(command.getAction());
        intent.putExtra(CUSTOM_DATA, data);

        if (isExternalSignals) mStableContext.sendBroadcast(intent);
        else LocalBroadcastManager.getInstance(mStableContext.obtainAppContext()).sendBroadcast(intent);
    }

    /* LocalBroadcast sender with customizable params */
    protected void sendLocalBroadcast(Broadcastable command, Bundle data) {
        Intent i = new Intent(command.getAction());
        i.putExtras(data);

        LocalBroadcastManager.getInstance(mStableContext.obtainAppContext()).sendBroadcast(i);
    }
}
