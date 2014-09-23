package mobi.anoda.archinamon.kernel.persefone.service.async;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentSkipListMap;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.async.AbstractAsyncTask;
import mobi.anoda.archinamon.kernel.persefone.network.processor.RESTSignal;
import mobi.anoda.archinamon.kernel.persefone.network.processor.SignalProcessor;
import mobi.anoda.archinamon.kernel.persefone.service.wakeful.WakefulIntentService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.impl.ServiceChannel;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
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
    protected volatile StableContext mStableContext;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractAsyncServer(String name) {
        super(name);
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
            taskInstance.prepare();
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

    @Implement
    protected final void onAction(@NonNull String action, @Nullable Intent data) {
    }
}
