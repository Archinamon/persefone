package mobi.anoda.archinamon.kernel.persefone.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.NetworkState;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * author: Archinamon
 * project: Wallpapers2
 */
public abstract class AbstractService extends Service {

    public class GraphBinder extends Binder {
    }

    private final          String              TAG              = Common.obtainClassTag(this);
    protected final        BroadcastFilter     fActionsFilter   = new BroadcastFilter();
    protected final        List<Runnable>      mSessionTasks    = new ArrayList<>();
    protected final        IBinder             mBinder          = new GraphBinder();
    protected volatile StableContext mStableContext;
    protected          BroadcastBus  mBroadcastBusDelegate;

    public void listenFor(Broadcastable action) {
        fActionsFilter.addAction(action);
    }

    public BroadcastBus getBroadcastBusDelegate() {
        return this.mBroadcastBusDelegate;
    }

    @Implement
    public IBinder onBind(Intent intent) {
        initSession();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCreate() {
        mStableContext = StableContext.Impl.instantiate(this);
        super.onCreate();

        initSession();

        mBroadcastBusDelegate = new BroadcastBus(mStableContext);
        mBroadcastBusDelegate.register(fActionsFilter);
    }

    @Override
    public void onDestroy() {
        mBroadcastBusDelegate.unregister();

        super.onDestroy();
    }

    protected synchronized final void addTask(Runnable task) {
        if (NetworkState.obtain(mStableContext).assertInternetAccess(false))
            task.run();
        else
            mSessionTasks.add(task);
    }

    protected synchronized void initSession() {
        for (Runnable task : mSessionTasks) {
            task.run();
        }
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }
}
