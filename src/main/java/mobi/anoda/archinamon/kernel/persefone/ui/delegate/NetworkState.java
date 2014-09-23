package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.IntentCompat;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.receiver.AbstractReceiver;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.signal.impl.ServiceChannel;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.NoInternetDialog;

/**
 * Created by matsukov-ea on 22.09.2014.
 */
@SuppressWarnings("FinalStaticMethod")
public final class NetworkState {

    private static final Object STATIC_LOCK = new Object();
    private static volatile NetworkState                      INSTANCE;
    private static          Class<? extends AbstractActivity> sfOnForceLogoutScreen;
    private static          Class<? extends AbstractDialog>   sfOnServerOfflineScreen;
    private                 StableContext                     mStableContext;
    private                 UiAffectionChain                  mAsyncChainBinder;
    private                 DialogLauncher                    mAlertLauncher;

    private /*synthetic*/ final AbstractReceiver mErrorsReceiver = new AbstractReceiver() {


        @Implement
        public void onReceive(@NonNull final String action, @Nullable Intent data) {
            if (NetworkNotification.FORCE_LOGOUT.isEqual(action)) {
                if (sfOnForceLogoutScreen != null) startWorkflow(sfOnForceLogoutScreen);
            } else if (NetworkNotification.ALERT_NO_INTERNET.isEqual(action)) {
                openPopup(NoInternetDialog.class, mStableContext.getString(R.string.no_internet_access));
                informError(null);
            } else if (NetworkNotification.ALERT_EXCEPTION.isEqual(action)) {
                final ErrorReport report = data != null ? data.<ErrorReport>getParcelableExtra(ServiceChannel.KEY_DATA) : null;
                informError(report);
            } else if (NetworkNotification.INTERNET_ACCESS_GRANTED.isEqual(action)) {
                if (mAsyncChainBinder != null)  mAsyncChainBinder.untwistStack();
            }
        }
    };

    public static NetworkState obtain(StableContext stableContext) {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new NetworkState(stableContext);
            }
        }

        return INSTANCE;
    }

    /**
     * @hide
     */
    public final static void setDefaultOnForceLogoutScreen(Class<? extends AbstractActivity> klass) throws ClassNotFoundException, IllegalAccessException {
        if (klass == null)
            throw new ClassNotFoundException("Cannot find requested class declaration");
        if (sfOnForceLogoutScreen != null)
            throw new IllegalAccessException("Cannot assign new value to a final variable");

        sfOnForceLogoutScreen = klass;
    }

    /**
     * @hide
     */
    public final static void setDefaultOnServerOfflineScreen(Class<? extends AbstractDialog> klass) throws ClassNotFoundException, IllegalAccessException {
        if (klass == null)
            throw new ClassNotFoundException("Cannot find requested class declaration");
        if (sfOnServerOfflineScreen != null)
            throw new IllegalAccessException("Cannot assign new value to a final variable");

        sfOnServerOfflineScreen = klass;
    }

    private NetworkState(@NonNull StableContext stableContext) {
        this.mStableContext = stableContext;
        this.mAlertLauncher = new DialogLauncher(mStableContext);
    }

    public AbstractReceiver getNetworkErrorProcessor() {
        return this.mErrorsReceiver;
    }

    public void linkWithAsyncChainBinder(UiAffectionChain binder) {
        this.mAsyncChainBinder = binder;
    }

    public void unlinkFromAsyncChainBinder() {
        this.mAsyncChainBinder = null;
    }

    public boolean assertInternetAccess(boolean inform) {
        final boolean status = isAccessAllowed();
        if (!status & inform) openPopup(NoInternetDialog.class, mStableContext.getString(R.string.no_internet_access));

        return status;
    }

    private void informError(ErrorReport report) {
        if (report != null) {
            String errorMsg = report.getMessage();
            mStableContext.shoutToast(errorMsg);
        }

        if (report != null && report.getStatus() == 403)
            openPopup(sfOnServerOfflineScreen, null);
    }

    private void startWorkflow(Class c) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        mStableContext.startActivity(intent);
    }

    private void openPopup(Class<? extends AbstractDialog> dialogClass, String message) {
        if (!mStableContext.isUiContextRegistered()) return;
        mAlertLauncher.openPopup(dialogClass, message);
    }

    private boolean isAccessAllowed() {
        return State.svAccessState == State.ACCESS_GRANTED || State.svAccessState == State.ACCESS_UNKNOWN;
    }
}
