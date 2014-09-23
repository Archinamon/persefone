package mobi.anoda.archinamon.kernel.persefone.ui.async.binder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractIntentService.RendezvousBinder;
import mobi.anoda.archinamon.kernel.persefone.service.async.AbstractAsyncServer;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.NetworkState;

/**
 * Created by matsukov-ea on 22.09.2014.
 */
public final class UiAffectionChain {

    private final          Object              MUTEX            = new Object();
    protected static final Stack<AsyncRequest> POSTPONED_CALLS  = new Stack<>();
    private final          List<OnServerReady> mServerListeners = new ArrayList<>();
    private final StableContext mStableContext;
    private final NetworkState  mNetworkWatchdog;

    protected RendezvousBinder                     mServerBinder;
    protected Class<? extends AbstractAsyncServer> mAsyncServiceImpl;
    protected volatile boolean           mIsBound    = false;
    private            ServiceConnection mConnection = new ServiceConnection() {

        @Implement
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServerBinder = (RendezvousBinder) service;
            mIsBound = true;

            if (mServerListeners.size() > 0) {
                for (OnServerReady listener : mServerListeners) {
                    listener.onBind();
                }
            }

            if (mNetworkWatchdog.assertInternetAccess(false))
                untwistStack();
        }

        @Implement
        public void onServiceDisconnected(ComponentName className) {
            mServerBinder = null;
            mIsBound = false;

            if (mServerListeners.size() > 0) {
                for (OnServerReady listener : mServerListeners) {
                    listener.onDisconnect();
                }
            }
        }
    };

    public UiAffectionChain(StableContext stableContext) {
        this.mNetworkWatchdog = NetworkState.obtain(stableContext);
        this.mStableContext = stableContext;
    }

    public final void setServerListener(OnServerReady l) {
        mServerListeners.add(l);

        if (mIsBound) {
            l.onBind();
        }
    }

    public final void removeServerListener(OnServerReady l) {
        mServerListeners.remove(l);
        l.onDisconnect();
    }

    public void doBindService(Class<? extends AbstractAsyncServer> service) {
        mAsyncServiceImpl = service;
        mNetworkWatchdog.linkWithAsyncChainBinder(this);
        mStableContext.bindService(new Intent(mStableContext.obtainAppContext(), service), mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinding randevu binder always processing in abstract instance
     */
    public void doUnbindService() {
        if (mIsBound) {
            mStableContext.unbindService(mConnection);
            mNetworkWatchdog.unlinkFromAsyncChainBinder();
            mConnection.onServiceDisconnected(mStableContext.getInfo());
        }
    }

    /* Helper for asynchronous starting REST requests */
    private void startAsyncServer(AsyncRequest request) {
        AsyncRequest.send(mStableContext.obtainAppContext(), request, mAsyncServiceImpl);

        if (mServerListeners.size() > 0) {
            for (OnServerReady listener : mServerListeners) {
                listener.onRendezvous(request);
            }
        }
    }

    /* Helper to postpone REST tasks */
    public void postponeRequest(AsyncRequest request) {
        if (mNetworkWatchdog.assertInternetAccess(false) && mIsBound) {
            startAsyncServer(request);
        } else {
            addCallToStack(request);
        }
    }

    public void addCallToStack(AsyncRequest request) {
        mNetworkWatchdog.assertInternetAccess(true);
        synchronized (MUTEX) {
            POSTPONED_CALLS.push(request);
        }
    }

    /* Helper for untwisting postponed REST tasks */
    public void untwistStack() {
        if (!POSTPONED_CALLS.empty()) {
            synchronized (MUTEX) {
                while (!POSTPONED_CALLS.empty()) {
                    AsyncRequest request = POSTPONED_CALLS.pop();
                    AsyncRequest.send(mStableContext.obtainAppContext(), request, mAsyncServiceImpl);

                    if (mServerListeners.size() > 0) {
                        for (OnServerReady listener : mServerListeners) {
                            listener.onRendezvous(request);
                        }
                    }
                }
            }
        }
    }
}
