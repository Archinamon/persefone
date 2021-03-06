package mobi.anoda.archinamon.kernel.persefone.network.processor;

import android.support.annotation.NonNull;
import java.io.InvalidObjectException;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
@SuppressWarnings("FinalStaticMethod")
public final class InterruptSequencer implements IStrategyInterrupt {

    public static interface LocalSequence {

        void noInternetThaw();

        void exceptionThaw(int code, String msg);

        void fatalThaw();
    }

    public static interface Informable {

        void informContext();
    }

    private static final Object STATIC_MUTEX = new Object();
    private static volatile InterruptSequencer INSTANCE;
    private                 LocalSequence      mApplicationSequence;
    private                 Informable         mInformer;

    public static InterruptSequencer getInstance() {
        synchronized (STATIC_MUTEX) {
            if (INSTANCE == null) {
                INSTANCE = new InterruptSequencer();
            }

            return INSTANCE;
        }
    }

    public static final void init(LocalSequence sequence) throws InvalidObjectException {
        if (INSTANCE == null) {
            throw new InvalidObjectException("Should be initiated first");
        }

        INSTANCE.mApplicationSequence = sequence;
    }

    public static final void addInformer(Informable i) {
        INSTANCE.mInformer = i;
    }

    public static final void releaseInformer() {
        INSTANCE.mInformer = null;
    }

    private InterruptSequencer() {}

    @Implement
    public final void jump(@NonNull StableContext context, Class<? extends AbstractActivity> lngjump_to) {
        ActivityLauncher launcher = new ActivityLauncher(context);
        launcher.switchWorkflow(lngjump_to);
    }

    @Implement
    public final void fatal(Throwable fatalExc) {
        throw new RuntimeException(fatalExc);
    }

    @Implement
    public final void operate(ErrorReport report) {
        if (mInformer != null) {
            mInformer.informContext();
        }

        final int obtained = report.getStatus();
        if (obtained == ErrorReport.NO_INTERNET) {
            mApplicationSequence.noInternetThaw();
            return;
        }

        if (ErrorReport.FATALS.contains(obtained)) {
            mApplicationSequence.fatalThaw();
            return;
        }

        mApplicationSequence.exceptionThaw(obtained, report.getMessage());
    }
}
