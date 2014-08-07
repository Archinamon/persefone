package mobi.anoda.archcore.persefone.services;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mobi.anoda.archcore.persefone.AnodaApplicationDelegate;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.network.AbstractAsyncTask;
import mobi.anoda.archcore.persefone.network.processor.RESTSignal;
import mobi.anoda.archcore.persefone.network.processor.SignalProcessor;
import mobi.anoda.archcore.persefone.signals.Broadcastable;
import mobi.anoda.archcore.persefone.signals.Channel;
import mobi.anoda.archcore.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archcore.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbstractAsyncServer extends AbstractIntentService {

    protected static final class SignalMap {

        final static ConcurrentSkipListMap<Channel, ConcurrentSkipListMap<String, Class<? extends AbstractAsyncTask>>> sTaskMap;

        static {
            sTaskMap = new ConcurrentSkipListMap<>();
            sTaskMap.put(Channel.CALL_REST_API, create());
            sTaskMap.put(Channel.CALL_SOCIAL_API, create());
        }

        static ConcurrentSkipListMap<String, Class<? extends AbstractAsyncTask>> create() {
            return new ConcurrentSkipListMap<>();
        }

        static Class<? extends AbstractAsyncTask> getTaskByGate(Channel gate, String what) {
            return sTaskMap.get(gate)
                           .get(what);
        }

        public static void add(Channel gate, Broadcastable callback, Class<? extends AbstractAsyncTask> task) {
            sTaskMap.get(gate).putIfAbsent(callback.getAction(), task);
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

    @Implement protected final void onAction(@Nonnull String action, @Nullable Intent data) {}
}
