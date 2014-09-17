package mobi.anoda.archinamon.kernel.persefone.network.async;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.widget.Toast;
import com.google.common.collect.ImmutableMap;
import org.apache.http.message.BasicNameValuePair;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.support.annotation.NonNull;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.model.NetworkModel;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.json.IJson;
import mobi.anoda.archinamon.kernel.persefone.network.json.JSONHashMap;
import mobi.anoda.archinamon.kernel.persefone.network.json.Projection;
import mobi.anoda.archinamon.kernel.persefone.network.operations.AbstractNetworkOperation;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.network.processor.IStrategyInterrupt;
import mobi.anoda.archinamon.kernel.persefone.network.processor.InterruptSequencer;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.signals.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.ProgressDialog;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 */
public abstract class AbstractAsyncTask<Progress, Result> extends CoreAsyncTask<Object, Progress, Result> implements Runnable, ResourceAccess {

    private static final String  TAG                    = AbstractAsyncTask.class.getSimpleName();
    private static final String  TEST_SOCKET            = "utcnist.colorado.edu";
    private static final long    DISMISS_PROGRESS_DELAY = 5000L;
    private static final int     STATUS_UNAUTHORIZED    = 401;
    private final        Object  MUTEX                  = new Object();
    private volatile     int     mInternetAccess        = 1;
    private              boolean mIsSilent              = false;
    private final        Handler mHandler               = new Handler();
    protected     AbstractActivity                mUiContext;
    protected     AbstractService                 mServiceContext;
    protected     AbstractDialog                  mProgressDialog;
    protected     NetworkModel                    mPropagatorModel;
    protected     Class<? extends AbstractDialog> mProgressTarget;
    protected     AbstractNetworkOperation        mOperation;
    protected     Broadcastable                   mActionCallback;
    protected     APIErrorCode                    mErrorTranslation;
    protected     IStrategyInterrupt              mErrorCallback;
    private       List<BasicNameValuePair>        mCompiledValuableModel;
    private       String                          mActualAction;

    public AbstractAsyncTask() {
    }

    public final void init(AbstractActivity context) {
        mUiContext = context;
        mProgressTarget = ProgressDialog.class;
        mErrorCallback = InterruptSequencer.getInstance();

        CoreAsyncTask.sWakeLock = CoreAsyncTask.getLock(context);

        onPostInit();
    }

    public final void init(AbstractService context) {
        mUiContext = null;
        mServiceContext = context;
        mProgressTarget = ProgressDialog.class;
        mErrorCallback = InterruptSequencer.getInstance();

        CoreAsyncTask.sWakeLock = CoreAsyncTask.getLock(context);

        onPostInit();
    }

    /**
     * Internal callback to finish app-demand initialization
     */
    protected abstract void onPostInit();

    /* PARAMETRIZATION */

    public final synchronized void dispatchSpinner() {
        mIsSilent = true;
    }

    public final synchronized void applySpinner(Class<? extends AbstractDialog> popupSpinner) {
        if (popupSpinner != null) {
            this.mIsSilent = false;
            this.mProgressTarget = popupSpinner;
        }
    }

    public final synchronized void applySpinner(AbstractDialog popupSpinner) {
        if (popupSpinner != null) {
            this.mIsSilent = false;
            this.mProgressDialog = popupSpinner;
        }
    }

    public final synchronized void connectInputModel(NetworkModel model) {
        this.mPropagatorModel = model;
    }

    public final synchronized void coherence(List<BasicNameValuePair> chainedProjection) {
        this.mCompiledValuableModel = chainedProjection;
    }

    public final synchronized void defineAction(String action) {
        this.mActualAction = action;
    }

    /* CALLBACKS */

    @Override
    protected void onPreExecute() {
        if (!mIsSilent) {
            if (mProgressDialog == null && mUiContext != null) {
                mProgressDialog = mUiContext.openPopup(mProgressTarget);
            }
        }

        new Thread(this).start();
    }

    protected final void post() {
        final int modelProjectionSize = mCompiledValuableModel.size();
        /*volatile*/
        BasicNameValuePair[] coherentProjection = new BasicNameValuePair[modelProjectionSize];
        mCompiledValuableModel.toArray(coherentProjection);//mutation
        mOperation.addValues(coherentProjection);
    }

    protected abstract Result onResponse(IJson response);

    @Override
    protected void onPostExecute(@Nullable Result result) {
        dismissLoadingPopup();
        deliverResult(result);
    }

    @SuppressWarnings("unchecked")
    private synchronized void deliverResult(Result result) {
        if (mActionCallback == null) {
            return;
        }

        if (result instanceof ArrayList) {
            sendBroadcast(mActionCallback, (ArrayList<Parcelable>) result);
        } else if (result instanceof Bundle) {
            sendBroadcast(mActionCallback, (Bundle) result);
        } else if (result instanceof Parcelable) {
            sendBroadcast(mActionCallback, (Parcelable) result);
        } else {
            sendBroadcast(mActionCallback);
        }
    }

    protected synchronized final boolean checkServerError(IJson result) {
        if (result == null) {
            ErrorReport report = mOperation.obtainErrorReport();
            NetworkNotification response = NetworkNotification.ALERT_EXCEPTION;

            if (report != null) {
                if (report.getStatus() == STATUS_UNAUTHORIZED) {
                    response = NetworkNotification.FORCE_LOGOUT;
                }
            }

            reportError(response, report);
            this.cancel(true);
            return true;
        }

        return false;
    }

    @Override
    protected void onCancelled(Result result) {
        //if we've obtained any data from request
        // let's send it to requester!
        if (result != null) {
            deliverResult(result);
        }

        super.onCancelled(result);
    }

    @Override
    protected final void onCancelled() {
        if (mInternetAccess != 1) {
            reportError(NetworkNotification.ALERT_NO_INTERNET, null);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dismissLoadingPopup();
        super.finalize();
    }

    protected final void shoutToast(final String message) {
        mHandler.post(new Runnable() {

            @Implement
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected final void shoutToast(@StringRes final int message) {
        mHandler.post(new Runnable() {

            @Implement
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected final void transcendError(JSONHashMap data) {
        ImmutableMap<String, String> error = data.getError();

        final String code;
        final String msg;
        if (error != null) {
            String[] errStr = new String[2];
            translateError(error, errStr);

            code = errStr[0];
            msg = errStr[1];
        } else {
            code = "-1";
            msg = getString(R.string.error_default);
        }

        mOperation.buildErrorReport(code, msg);
        ErrorReport report = mOperation.obtainErrorReport();
        reportError(NetworkNotification.ALERT_EXCEPTION, report);
    }

    protected final void translateError(@NonNull ImmutableMap<String, String> error, String[] retData) {
        String code = error.get(Projection.Error.ERROR_CODE);
        if (code != null) {
            retData[0] = code;
            retData[1] = getString(mErrorTranslation.getMessage(code));
            return;
        }

        retData[0] = "-1";
        retData[1] = error.get(Projection.Error.ERROR_MESSAGE);
    }

    protected final NetworkModel getInputModel() {
        return this.mPropagatorModel;
    }

    protected final <T extends Parcelable> void notifyService(Class<? extends Service> service, Broadcastable a, T data) {
        Intent intent = new Intent(getContext(), service);
        intent.setAction(a.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        getContext().startService(intent);
    }

    protected final <T extends Parcelable> void notifyService(Class<? extends Service> service, Broadcastable a, ArrayList<T> data) {
        Intent intent = new Intent(getContext(), service);
        intent.setAction(a.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        getContext().startService(intent);
    }

    protected final <T extends Parcelable> void sendBroadcast(Broadcastable a, ArrayList<T> data) {
        Intent i = new Intent(a.getAction());
        i.putParcelableArrayListExtra(Broadcastable.KEY_DATA, data);

        getContext().sendBroadcast(i);
    }

    protected final <T extends Parcelable> void sendBroadcast(Broadcastable a, T data) {
        Intent i = new Intent(a.getAction());
        i.putExtra(Broadcastable.KEY_DATA, data);

        getContext().sendBroadcast(i);
    }

    protected final void sendBroadcast(Broadcastable a, Bundle data) {
        Intent i = new Intent(a.getAction());
        i.putExtra(Broadcastable.KEY_DATA, data);

        getContext().sendBroadcast(i);
    }

    protected final void sendBroadcast(Broadcastable a) {
        Intent i = new Intent(a.getAction());

        getContext().sendBroadcast(i);
    }

    protected final <T extends Parcelable> void reportError(NetworkNotification a, T data) {
        Intent i = new Intent(a.getAction());
        i.putExtra(NetworkNotification.KEY_DATA, data);

        getContext().sendBroadcast(i);

        dismissLoadingPopup();
    }

    protected boolean checkAction(Broadcastable isAction) {
        return isAction.isEqual(mActualAction);
    }

    @Implement
    public final String getString(int id) {
        return getContext().getString(id);
    }

    @Implement
    public final String getString(int id, Object... modifiers) {
        return getContext().getString(id, modifiers);
    }

    protected final void logError(String tag, Exception e) {
        LogHelper.println_error(tag, e);
    }

    @Implement
    public void run() {
        InputStream i = null;

        synchronized (MUTEX) {
            try {
                Socket s = new Socket(TEST_SOCKET, 37);
                i = s.getInputStream();
                Scanner scan = new Scanner(i);

                while (scan.hasNextLine()) {
                    System.out.println(scan.nextLine());
                    mInternetAccess = 1;
                }
            } catch (Exception e) {
                mInternetAccess = 0;
            } finally {
                if (i != null) {
                    try {
                        i.close();
                    } catch (Exception ex) {
                        LogHelper.println_error(TAG, ex);
                    }
                }
            }
        }

        checkInternet();
    }


    protected final synchronized void checkInternet() {
        if (State.svAccessState != State.ACCESS_GRANTED || mInternetAccess != 1) {
            if (mProgressDialog != null && mProgressDialog.getShowsDialog()) {
                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                    @Implement
                    public void run() {
                        dismissLoadingPopup();
                    }
                }, DISMISS_PROGRESS_DELAY, TimeUnit.MILLISECONDS);
            }
            cancel(true);
        }
    }

    protected final synchronized void dismissLoadingPopup() {
        if (mProgressDialog != null) {
            while (!mProgressDialog.isResumed());
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    protected final Context getContext() {
        return mUiContext != null ? mUiContext : mServiceContext;
    }
}
