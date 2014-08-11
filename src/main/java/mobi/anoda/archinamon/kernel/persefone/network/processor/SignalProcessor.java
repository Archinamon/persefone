package mobi.anoda.archinamon.kernel.persefone.network.processor;

import java.io.Serializable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class SignalProcessor implements Serializable {

    public static final  String TAG              = SignalProcessor.class.getSimpleName();
    private static final long   serialVersionUID = -17864234627988L;
    /*default*/ static final              List<RESTSignal> sSubscribedSignals = Collections.synchronizedList(new ArrayList<RESTSignal>());
    /*default*/ static transient volatile AtomicInteger    sRefCount          = new AtomicInteger(sSubscribedSignals.size());
    //inner data
    private volatile static SignalProcessor  INSTANCE;

    public synchronized static SignalProcessor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SignalProcessor();
        }

        return INSTANCE;
    }

    private SignalProcessor() {
    }

    public final /*synthetic*/ synchronized void subscribe(@Nonnull RESTSignal signal) {
        sSubscribedSignals.add(signal);
        sRefCount.incrementAndGet();
    }

    public final /*synthetic*/ synchronized void post() {
        for (RESTSignal signal : sSubscribedSignals) {
            sRefCount.decrementAndGet();
            signal.eval();
        }

        invalidate();
    }

    private static synchronized void invalidate() {
        Thread updater = new Thread(new Runnable() {

            @Implement
            public void run() {
                if (sRefCount.get() <= 0) sSubscribedSignals.clear(); else sSubscribedSignals.remove(sRefCount.get());
            }
        });
        updater.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Implement
            public void uncaughtException(Thread thread, Throwable ex) {
                throw new RuntimeException(ex);
            }
        });
        updater.setPriority(Thread.MAX_PRIORITY);
        updater.setDaemon(true);
        updater.start();
    }
}
