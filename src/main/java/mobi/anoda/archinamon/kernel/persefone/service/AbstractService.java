package mobi.anoda.archinamon.kernel.persefone.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.service.async.AbstractAsyncServer;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.signals.AsyncReceiver;
import mobi.anoda.archinamon.kernel.persefone.signals.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signals.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signals.Permission;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.WordUtils;

/**
 * author: Archinamon
 * project: Wallpapers2
 */
public abstract class AbstractService extends Service {

    public class GraphBinder extends Binder {

        public void onAttach(AbstractActivity context) {
            attachContext(context);
        }
    }

    public static final    String              CUSTOM_DATA      = ".custom:key_data";
    protected static final Stack<AsyncRequest> POSTPONED_CALLS  = new Stack<>();
    protected final        BroadcastFilter     FILTER           = new BroadcastFilter();
    protected final        List<OnServerReady> mServerListeners = new ArrayList<>();
    protected final        List<Runnable>      mSessionTasks    = new ArrayList<>();
    protected final        IBinder             mBinder          = new GraphBinder();
    private final          String              TAG              = Common.obtainClassTag(this);
    private final          Object              MUTEX            = new Object();
    protected volatile AbstractActivity                     mUiContext;
    protected volatile AnodaApplicationDelegate             mAppDelegate;
    protected          Class<? extends AbstractAsyncServer> mAsyncServiceImpl;
    protected          AsyncReceiver                        mServerListener;
    private final BroadcastReceiver mMainAsyncReceiver = new BroadcastReceiver() {

        @Implement
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                assert action != null;

                if (mServerListener != null) {
                    mServerListener.onReceive(action, intent);
                }
            } catch (Exception any) {
                mServerListener.onException(any);
            }
        }
    };

    @Implement
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppDelegate = (AnodaApplicationDelegate) getApplication();
        untwistStack();
        initSession();

        if (FILTER.countActions() > 0)
            registerReceiver(mMainAsyncReceiver, FILTER);
    }

    @Override
    public void onDestroy() {
        if (FILTER.countActions() > 0)
            unregisterReceiver(mMainAsyncReceiver);

        super.onDestroy();
    }

    public final void registerAsyncReceiver(AsyncReceiver impl) {
        mServerListener = impl;
    }

    @Nullable
    protected synchronized final Context obtainUiContext() {
        return mUiContext != null ? mUiContext : getApplicationContext();
    }

    protected synchronized void attachContext(AbstractActivity context) {
        mAppDelegate = context.getAppDelegate();
        mUiContext = context;

        untwistStack();
        initSession();
    }

    protected synchronized final void addTask(Runnable task) {
        if (accessAllowed())
            task.run();
        else
            mSessionTasks.add(task);
    }

    protected synchronized void initSession() {
        for (Runnable task : mSessionTasks) {
            task.run();
        }
    }

    /* Helper to announce message to user */
    public void shoutToast(String msg) {
        if (WordUtils.isEmpty(msg))
            return;

        Toast info = Toast.makeText(this, msg, Toast.LENGTH_SHORT);

        int y = info.getYOffset();
        int x = info.getXOffset();

        info.setGravity(Gravity.TOP | Gravity.CENTER, x / 2, y);
        info.show();
    }

    protected void setServer(Class<? extends AbstractAsyncServer> service) {
        mAsyncServiceImpl = service;
    }

    /* Helper to postpone REST tasks */
    public void postponeRequest(AsyncRequest request) {
        if (accessAllowed()) {
            startAsyncServer(request);
        } else {
            addCallToStack(request);
        }
    }

    public void startActivity(Broadcastable action) {
        startActivity(new Intent(action.getAction()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /* Simple broadcast sender*/
    public void sendBroadcast(Broadcastable command) {
        sendBroadcast(new Intent(command.getAction()));
    }

    public void sendOrderedBroadcast(Broadcastable command, Permission permission) {
        sendOrderedBroadcast(new Intent(command.getAction()), permission.getPermission());
    }

    /* Broadcast sender with customizable params */
    protected void sendBroadcast(Broadcastable command, Bundle data) {
        sendBroadcast(new Intent(command.getAction()).putExtras(data));
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    /* Helper for asynchronous starting REST requests */
    private void startAsyncServer(AsyncRequest request) {
        AsyncRequest.send(this, request, mAsyncServiceImpl);

        if (mServerListeners.size() > 0) {
            for (OnServerReady listener : mServerListeners) {
                listener.onRendezvous(request);
            }
        }
    }

    protected void addCallToStack(AsyncRequest request) {
        synchronized (MUTEX) {
            POSTPONED_CALLS.push(request);
        }
    }

    /* Helper for untwisting postponed REST tasks */
    private void untwistStack() {
        if (!POSTPONED_CALLS.empty()) {
            synchronized (MUTEX) {
                while (!POSTPONED_CALLS.empty()) {
                    AsyncRequest request = POSTPONED_CALLS.pop();
                    AsyncRequest.send(this, request, mAsyncServiceImpl);

                    if (mServerListeners.size() > 0) {
                        for (OnServerReady listener : mServerListeners) {
                            listener.onRendezvous(request);
                        }
                    }
                }
            }
        }
    }

    protected final boolean accessAllowed() {
        return State.svAccessState == State.ACCESS_GRANTED || State.svAccessState == State.ACCESS_UNKNOWN;
    }
}
