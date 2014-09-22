package mobi.anoda.archinamon.kernel.persefone.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.service.async.AbstractAsyncServer;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Permission;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
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

    protected static final Stack<AsyncRequest> POSTPONED_CALLS  = new Stack<>();
    protected final        BroadcastFilter     fActionsFilter   = new BroadcastFilter();
    protected final        List<OnServerReady> mServerListeners = new ArrayList<>();
    protected final        List<Runnable>      mSessionTasks    = new ArrayList<>();
    protected final        IBinder             mBinder          = new GraphBinder();
    private final          String              TAG              = Common.obtainClassTag(this);
    private final          Object              MUTEX            = new Object();
    protected volatile StableContext                        mStableContext;
    protected          BroadcastBus                         mBroadcastBusDelegate;
    protected          Class<? extends AbstractAsyncServer> mAsyncServiceImpl;

    public void listenFor(Broadcastable action) {
        fActionsFilter.addAction(action);
    }

    public BroadcastBus getBroadcastBusDelegate() {
        return this.mBroadcastBusDelegate;
    }

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
        mStableContext = StableContext.instantiate(this);
        super.onCreate();

        untwistStack();
        initSession();

        mBroadcastBusDelegate = new BroadcastBus(mStableContext);
        mBroadcastBusDelegate.register(fActionsFilter);
    }

    @Override
    public void onDestroy() {
        mBroadcastBusDelegate.unregister();

        super.onDestroy();
    }

    protected synchronized void attachContext(AbstractActivity context) {
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

    public void startActivity(Broadcastable action, Bundle data) {
        startActivity(new Intent(action.getAction())
                              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                              .putExtras(data));
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
