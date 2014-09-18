package mobi.anoda.archinamon.kernel.persefone.service.async;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentSkipListMap;
import android.support.annotation.NonNull;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.async.AbstractAsyncTask;
import mobi.anoda.archinamon.kernel.persefone.network.processor.RESTSignal;
import mobi.anoda.archinamon.kernel.persefone.network.processor.SignalProcessor;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.service.wakeful.WakefulIntentService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.impl.ServiceChannel;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbstractAsyncServer extends WakefulIntentService {

    protected static final class SignalMap {

        final static ConcurrentSkipListMap<ServiceChannel, ConcurrentSkipListMap<String, Class<? extends AbstractAsyncTask>>> sTaskMap;

        static {
            sTaskMap = new ConcurrentSkipListMap<>();
            sTaskMap.put(ServiceChannel.CALL_REST_API, create());
            sTaskMap.put(ServiceChannel.CALL_SOCIAL_API, create());
        }

        static ConcurrentSkipListMap<String, Class<? extends AbstractAsyncTask>> create() {
            return new ConcurrentSkipListMap<>();
        }

        static Class<? extends AbstractAsyncTask> getTaskByGate(ServiceChannel gate, String what) {
            return sTaskMap.get(gate)
                           .get(what);
        }

        public static void add(ServiceChannel gate, Broadcastable callback, Class<? extends AbstractAsyncTask> task) {
            sTaskMap.get(gate)
                    .putIfAbsent(callback.getAction(), task);
        }
    }

    private static final String  TAG     = AbstractAsyncServer.class.getSimpleName();
    protected final      IBinder mBinder = new RendezvousBinder();
    protected volatile AnodaApplicationDelegate mAppDelegate;
    protected volatile AbstractActivity         mUiContext;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractAsyncServer(String name) {
        super(name);
    }

    /**
     * We implement strongly different logic of Service binding mechanism
     * The binding logic of core {@link AbstractService} is to handle {@link Context}
     * in usual sync way. Here we should build a bridge to handle rendezvous events from {@link Context}s
     *
     * @param activity
     */
    @Override
    public synchronized void attachContext(AbstractActivity activity) {
        mAppDelegate = activity.getAppDelegate();
        mUiContext = activity;

        if (mAppDelegate == null) {
            mAppDelegate = (AnodaApplicationDelegate) activity.getApplication();
        }
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected void unwindRequest(AsyncRequest request) {
        final SignalProcessor processor = getProcessor();
        try {
            Class<? extends AbstractAsyncTask> task = SignalMap.getTaskByGate(request.getGate(), request.getCommand());
            AbstractAsyncTask taskInstance = task.newInstance();
            if (mUiContext != null) taskInstance.init(mUiContext);
            else taskInstance.init(this);

            taskInstance.defineAction(request.getCommand());

            RESTSignal.Builder signal = new RESTSignal.Builder().bindCoherentTask(taskInstance)
                                                                .bindProjection(request.getPropagator())
                                                                .attachPopup(request.getPopupClass());
            if (request.isDaemon())
                signal.asDaemon();

            processor.subscribe(signal.build());
        } catch (UnsupportedEncodingException e) {
            LogHelper.println_error(TAG, e);
            throw new RuntimeException("error unwinding async request :: can't obtain callback");
        } catch (InstantiationException | IllegalAccessException e) {
            LogHelper.println_error(TAG, e);
            throw new RuntimeException("error unwinding async request :: can't instantiate task class");
        } finally {
            processor.post();
        }
    }

    private SignalProcessor getProcessor() {
        return SignalProcessor.getInstance();
    }

    @Implement protected final void onAction(@NonNull String action, @Nullable Intent data) {}
}
