package mobi.anoda.archinamon.kernel.persefone.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.receiver.AbstractReceiver;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractIntentService.RendezvousBinder;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.service.async.AbstractAsyncServer;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.impl.ServiceChannel;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.actionbar.ActionBarFactory;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable.FragmentState;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.NoInternetDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.MetricsHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.WordUtils;
import mobi.anoda.archinamon.kernel.persefone.utils.fonts.FontsHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.fonts.IAssetFont;

/**
 * author: Archinamon project: FavorMe
 */
public abstract class AbstractActivity<Controllable extends AbstractFragment & StateControllable> extends ActionBarActivity implements TaggedView {

    public static final    int                 RESULT_EDITED    = 0xedf;
    public static final    int                 RESULT_DELETED   = 0xdef;
    public static final    String              CUSTOM_DATA      = ".custom:key_data";
    protected static final Stack<AsyncRequest> POSTPONED_CALLS  = new Stack<>();
    private final          BroadcastFilter     fActionsFilter   = new BroadcastFilter();
    private final          List<OnServerReady> mServerListeners = new ArrayList<>();
    private final          String              TAG              = ((Object) this).getClass()
                                                                                 .getSimpleName();
    private final          Object              MUTEX            = new Object();
    private static Class<? extends AbstractActivity>    sfOnForceLogoutScreen;
    private static Class<? extends AbstractDialog>      sfOnServerOfflineScreen;
    private        StableContext                        mStableContext;
    protected      SoftKeyboard                         mKeyboardManagerDelegate;
    protected      BroadcastBus                         mBroadcastBusDelegate;
    protected      Class<? extends AbstractAsyncServer> mAsyncServiceImpl;
    protected      AbstractActivity                     mSelf;
    protected      RendezvousBinder                     mServerBinder;
    protected      LoaderCallbacks<Cursor>              mLoaderCallbacks;
    protected      ActionBarFactory                     mActionBar;
    protected      ActionBar                            mActionBarImpl;
    protected      int                                  mLoader;
    protected volatile boolean           mIsBound         = false;
    protected volatile boolean           isPaused         = true;
    private volatile   Controllable      mCurrentFragment = null;
    private final      AbstractReceiver  mErrorsReceiver  = new AbstractReceiver() {

        @Implement
        public void onReceive(@NonNull final String action, @Nullable Intent data) {
            if (data != null) {
                final ErrorReport report = data.getParcelableExtra(ServiceChannel.KEY_DATA);
                if (NetworkNotification.FORCE_LOGOUT.isEqual(action)) {
                    if (sfOnForceLogoutScreen != null)
                        switchWorkflow(sfOnForceLogoutScreen);
                } else if (NetworkNotification.ALERT_NO_INTERNET.isEqual(action)) {
                    openPopup(NoInternetDialog.class, getString(R.string.no_internet_access));
                    informError(null);
                } else if (NetworkNotification.ALERT_EXCEPTION.isEqual(action)) {
                    informError(report);
                } else if (NetworkNotification.INTERNET_ACCESS_GRANTED.isEqual(action)) {
                    untwistStack();
                }
            }
        }
    };
    private            ServiceConnection mConnection      = new ServiceConnection() {

        @Implement
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServerBinder = (RendezvousBinder) service;
            mServerBinder.onAttach(mSelf);

            mIsBound = true;

            if (mServerListeners.size() > 0) {
                for (OnServerReady listener : mServerListeners) {
                    listener.onBind();
                }
            }

            if (accessAllowed())
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

    /**
     * @hide
     */
    @SuppressWarnings("FinalStaticMethod")
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
    @SuppressWarnings("FinalStaticMethod")
    public final static void setDefaultOnServerOfflineScreen(Class<? extends AbstractDialog> klass) throws ClassNotFoundException, IllegalAccessException {
        if (klass == null)
            throw new ClassNotFoundException("Cannot find requested class declaration");
        if (sfOnServerOfflineScreen != null)
            throw new IllegalAccessException("Cannot assign new value to a final variable");

        sfOnServerOfflineScreen = klass;
    }

    public final AbstractReceiver getDefaultNetworkListener() {
        return this.mErrorsReceiver;
    }

    public void listenFor(Broadcastable action) {
        fActionsFilter.addAction(action);
    }

    public final ActionBar getActionBarImpl() {
        return this.mActionBarImpl;
    }

    protected /*virtual*/ void informError(ErrorReport report) {
        boolean transcend = true;
        if (mCurrentFragment != null && mCurrentFragment.getState() == FragmentState.ATTACHED) {
            transcend = mCurrentFragment.informError(report);
        }

        if (transcend && report != null) {
            String errorMsg = report.getMessage();
            shoutToast(errorMsg);
        }

        if (transcend && report != null && report.getStatus() == 1234)
            openPopup(sfOnServerOfflineScreen);
    }

    protected Loader<Cursor> initLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        LoaderManager manager = super.getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        } else {
            return manager.initLoader(id, params, mLoaderCallbacks);
        }
    }

    protected Loader<Cursor> restartLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        LoaderManager manager = super.getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        }

        return null;
    }

    protected void doBindService(Class<? extends AbstractAsyncServer> service) {
        mAsyncServiceImpl = service;
        super.bindService(new Intent(mSelf, service), mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinding randevu binder always processing in abstract instance
     */
    void doUnbindService() {
        if (mIsBound) {
            super.unbindService(mConnection);
            mConnection.onServiceDisconnected(super.getCallingActivity());
        }
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

    public final Class<?> getServerImpl() {
        return mAsyncServiceImpl;
    }

    protected enum PopupType {

        SMALL,
        LARGE
    }

    protected void buildAsPopup(PopupType type) {
        final int[] metrics = new int[2];
        final int marginTop = (int) ((48 * Common.getDensityMultiplier(this)) * 1.5f);
        final boolean isTablet = MetricsHelper.isTablet(this);

        switch (type) {
            case SMALL:
                MetricsHelper.calcSmallPopupMetrics(this, metrics);
                break;
            case LARGE:
                MetricsHelper.calcLargePopupMetrics(this, metrics);
                break;
        }

        LayoutParams params = super.getWindow()
                                   .getAttributes();
        params.height = metrics[0];
        params.width = metrics[1];
        params.alpha = 1f;
        params.dimAmount = .7f;
        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.y = isTablet ? marginTop << 2 : marginTop << 1;

        super.getWindow()
             .setFlags(LayoutParams.FLAG_DIM_BEHIND, LayoutParams.FLAG_DIM_BEHIND);
        super.getWindow()
             .setAttributes(params);
        super.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStableContext = StableContext.<AbstractActivity>instantiate(this);

        super.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        try {
            mActionBar = new ActionBarFactory(super.getSupportActionBar());
        } catch (IllegalAccessException e) {
            logError(e);
        }

        mKeyboardManagerDelegate = new SoftKeyboard(mStableContext);
        mKeyboardManagerDelegate.initTouchInterceptor();
        mKeyboardManagerDelegate.initKeyboardShowListener();

        mBroadcastBusDelegate = new BroadcastBus(mStableContext);
    }

    @Override
    protected void onResume() {
        if (mLoaderCallbacks != null) {
            initLoader(mLoader, null, mLoaderCallbacks);
        }

        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBroadcastBusDelegate.registerNetworkEventsForCurrentUiContext();
        mBroadcastBusDelegate.register(fActionsFilter);
    }

    @Override
    protected void onStop() {
        mBroadcastBusDelegate.unregisterNetworkEventsForCurrentUiContext();
        mBroadcastBusDelegate.unregister();
        doUnbindService();

        super.onStop();
    }

    public SoftKeyboard getKeyboardDelegate() {
        return this.mKeyboardManagerDelegate;
    }

    public BroadcastBus getBroadcastBusDelegate() {
        return this.mBroadcastBusDelegate;
    }

    protected void setContentView(int layout, IAssetFont typeface) {
        View v = getLayoutInflater().inflate(layout, null);
        super.setContentView(v);

        FontsHelper.applyFonts(v, FontsHelper.getCustomFont(this, typeface));
    }

    protected void setContentView(View layout, IAssetFont typeface) {
        super.setContentView(layout);

        FontsHelper.applyFonts(layout, FontsHelper.getCustomFont(this, typeface));
    }

    public final synchronized void registerControllable(Controllable fragment) {
        mCurrentFragment = fragment;
        syncState(FragmentState.INIT);
    }

    public final synchronized void syncState(FragmentState state) {
        if (mCurrentFragment == null) {
            LogHelper.println_verbose(TAG, "No fragment registered for AbstractStateMachine control");
        }

        switch (state) {
            case INIT:
            case DETACHED:
                mCurrentFragment.onSwitch();
                break;
            case ATTACHED:
                mCurrentFragment.onRemove();
                break;
            default:
                throw new IllegalStateException("Non-acceptable fragment state received");
        }

        LogHelper.println_verbose(TAG, state.name());
    }

    public final void exitFragment() {
        FragmentManager manager = super.getSupportFragmentManager();
        if (!manager.popBackStackImmediate()) {
            if (mCurrentFragment != null) {
                syncState(mCurrentFragment.getState());
            }
            exitActivity();
        }
    }

    public void startService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        super.startService(intent);
    }

    public void stopService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        super.stopService(intent);
    }

    /* Helper to postpone REST tasks */
    public void postponeRequest(AsyncRequest request) {
        if (accessAllowed() && !isPaused && mIsBound) {
            startAsyncServer(request);
        } else {
            addCallToStack(request);
        }
    }

    public String getTextFromView(@IdRes int viewId) {
        View v = super.findViewById(viewId);
        if (v instanceof TextView) {
            TextView view = (TextView) v;
            return view.getText()
                       .toString();
        } else {
            return v.toString();
        }
    }

    /* Helper to announce message to user */
    public void shoutToast(String msg) {
        if (WordUtils.isEmpty(msg))
            return;

        Toast info = Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT);

        int y = info.getYOffset();
        int x = info.getXOffset();

        info.setGravity(Gravity.TOP | Gravity.CENTER, x / 2, y);
        info.show();
    }

    /* Helper for opening new fragment */
    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, null);

        switchFragmentInternal(fragment, addToStack);

        return fragment;
    }

    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, Bundle params, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, addToStack);

        return fragment;
    }

    protected AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, Bundle params) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, true);

        return fragment;
    }

    private <TagFrmt extends AbstractFragment & TaggedView> void switchFragmentInternal(TagFrmt fragment, boolean isAddingToBackstack) {
        final String tag = fragment.getViewTag();
        FragmentManager manager = super.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (isAddingToBackstack) {
            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            transaction.addToBackStack(tag);
        }

        transaction.replace(R.id.fragment_view, fragment, tag);
        transaction.commit();

        if (mCurrentFragment != null) {
            syncState(mCurrentFragment.getState());
        }

        setServerListener(fragment);
    }

    /* Launch Popup dialog */
    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass) {
        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, null);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, Bundle params) {
        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, Parcelable data) {
        final Bundle params = new Bundle();
        params.putParcelable(CUSTOM_DATA, data);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractDialog.IEXTRA_MESSAGE, message);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, String title, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractDialog.IEXTRA_TITLE, title);
        params.putString(AbstractDialog.IEXTRA_MESSAGE, message);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    private <TagDialog extends AbstractDialog & TaggedView> TagDialog openPopupInternal(TagDialog popup) {
        final String tag = popup.getViewTag();
        FragmentManager manager = super.getSupportFragmentManager();

        if (!popup.isShowing(tag)) {
            popup.show(manager, tag);
            setServerListener(popup);
        }

        return popup;
    }

    public void startActivity(Broadcastable action) {
        super.startActivity(new Intent(action.getAction()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void startDisorderedActivity(Broadcastable action) {
        super.startActivity(new Intent(action.getAction()).setPackage(super.getPackageName()));
    }

    public void startDisorderedActivity(Broadcastable action, Bundle params) {
        Intent activity = new Intent(action.getAction());
        activity.setPackage(super.getPackageName());
        activity.putExtras(params);
        super.startActivity(activity);
    }

    public void startDisorderedActivity(Broadcastable action, Class<? extends Activity> activity) {
        Intent intent = new Intent(mSelf, activity).setClassName(super.getPackageName(), activity.getName())
                                                   .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                   .setAction(action.getAction())
                                                   .setPackage(super.getPackageName());
        super.startActivity(intent);
    }

    public void startDisorderedActivity(Broadcastable action, Class<? extends Activity> activity, Bundle params) {
        Intent intent = new Intent(mSelf, activity).setClassName(super.getPackageName(), activity.getName())
                                                   .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                   .setAction(action.getAction())
                                                   .setPackage(super.getPackageName())
                                                   .putExtras(params);
        super.startActivity(intent);
    }


    public void startActivity(Broadcastable action, Bundle params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(params);
        super.startActivity(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, Data params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(CUSTOM_DATA, params);
        super.startActivity(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, ArrayList<Data> params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putParcelableArrayListExtra(CUSTOM_DATA, params);
        super.startActivity(i);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void switchWorkflow(Class c, T data) {
        Intent intent = new Intent(mSelf, c);
        intent.putExtra(CUSTOM_DATA, data);

        startWorkflow(c, intent);
    }

    public void switchWorkflow(Class c) {
        startWorkflow(c, null);
    }

    /* Launch new top activity with anim */
    public void openActivityWithTaskRecreate(Class c) {
        Intent intent = new Intent(mSelf, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.startActivity(intent);
        super.overridePendingTransition(R.anim.grow_fade_in, R.anim.shrink_fade_out);
    }

    /* Switch activity with anim */
    public void enterActivity(Class c) {
        startActivity(c, null);
        super.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity */
    public void enterActivity(Class c, int data) {
        Intent intent = new Intent(mSelf, c);
        intent.putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void enterActivity(Class c, T data) {
        Intent intent = new Intent(mSelf, c);
        if (data instanceof Bundle) {
            intent.putExtras((Bundle) data);
        } else {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivity(c, intent);
        super.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void enterActivity(Class c, ArrayList<T> data) {
        Intent intent = new Intent(mSelf, c);
        intent.putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
        super.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result with anim */
    public <T extends Parcelable> void enterActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        super.startActivityForResult(intent, code);
        super.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        super.startActivityForResult(intent, code);
    }

    /* Switch activity for result without anim */
    public void openActivityForResult(Class c, int code, int data) {
        Intent intent = new Intent(mSelf, c);
        intent.putExtra(CUSTOM_DATA, data);

        super.startActivityForResult(intent, code);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, ArrayList<T> data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        super.startActivityForResult(intent, code);
    }

    /* Exit to concrete activity with anim */
    public void exitActivity(Class c) {
        if (c != null) {
            startActivity(c, null);
        }

        mKeyboardManagerDelegate.hideSoftInput();
        super.finish();
        super.overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Return to previous activity with anim */
    public void exitActivity() {
        mKeyboardManagerDelegate.hideSoftInput();
        super.finish();
        super.overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Transcend result to launcher-activity and finish with anim */
    public void deliverResult(int r, Intent i) {
        super.setResult(r, i);
        super.finish();
        super.overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Generalized REST errors parser */
    protected String parseError(ErrorReport report) {
        String error = "";
        if (report != null) {
            error = report.getMessage();
        }

        return error;
    }

    /* Helper to open new activity with anim */
    private void startActivity(Class c, Intent i) {
        Intent intent = i != null ? i : new Intent(mSelf, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.startActivity(intent);
    }

    /* Helper to open new activity with anim */
    private void startWorkflow(Class c, Intent i) {
        Intent intent = i != null ? i : new Intent(mSelf, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(intent);
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    /* Helper for asynchronous starting REST requests */
    private void startAsyncServer(AsyncRequest request) {
        AsyncRequest.send(mSelf, request, mAsyncServiceImpl);

        if (mServerListeners.size() > 0) {
            for (OnServerReady listener : mServerListeners) {
                listener.onRendezvous(request);
            }
        }
    }

    protected void addCallToStack(AsyncRequest request) {
        assertInternetAccess();
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
                    AsyncRequest.send(mSelf, request, mAsyncServiceImpl);

                    if (mServerListeners.size() > 0) {
                        for (OnServerReady listener : mServerListeners) {
                            listener.onRendezvous(request);
                        }
                    }
                }
            }
        }
    }

    public boolean assertInternetAccess() {
        final boolean status = accessAllowed();
        if (!status)
            openPopup(NoInternetDialog.class, getString(R.string.no_internet_access));

        return status;
    }

    private boolean accessAllowed() {
        return State.svAccessState == State.ACCESS_GRANTED || State.svAccessState == State.ACCESS_UNKNOWN;
    }
}
