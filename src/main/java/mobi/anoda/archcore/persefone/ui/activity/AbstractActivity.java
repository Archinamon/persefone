package mobi.anoda.archcore.persefone.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.flurry.android.FlurryAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import mobi.anoda.archcore.persefone.AnodaApplicationDelegate;
import mobi.anoda.archcore.persefone.R;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.network.State;
import mobi.anoda.archcore.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archcore.persefone.services.AbstractAsyncServer;
import mobi.anoda.archcore.persefone.services.AbstractIntentService.RendezvousBinder;
import mobi.anoda.archcore.persefone.services.AbstractService;
import mobi.anoda.archcore.persefone.services.AsyncRequest;
import mobi.anoda.archcore.persefone.services.NetworkNotification;
import mobi.anoda.archcore.persefone.signals.AsyncReceiver;
import mobi.anoda.archcore.persefone.signals.BroadcastFilter;
import mobi.anoda.archcore.persefone.signals.Broadcastable;
import mobi.anoda.archcore.persefone.signals.Channel;
import mobi.anoda.archcore.persefone.signals.Permission;
import mobi.anoda.archcore.persefone.ui.TaggedView;
import mobi.anoda.archcore.persefone.ui.actionbar.ActionBarFactory;
import mobi.anoda.archcore.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archcore.persefone.ui.activity.interfaces.StateControllable;
import mobi.anoda.archcore.persefone.ui.activity.interfaces.StateControllable.FragmentState;
import mobi.anoda.archcore.persefone.ui.dialog.AbstractPopup;
import mobi.anoda.archcore.persefone.ui.dialog.NoInternetPopup;
import mobi.anoda.archcore.persefone.ui.fragment.AbstractFragment;
import mobi.anoda.archcore.persefone.utils.Common;
import mobi.anoda.archcore.persefone.utils.LogHelper;
import mobi.anoda.archcore.persefone.utils.MetricsHelper;
import mobi.anoda.archcore.persefone.utils.WordUtils;
import mobi.anoda.archcore.persefone.utils.fonts.AssetFont;
import mobi.anoda.archcore.persefone.utils.fonts.FontsHelper;

/**
 * author: Archinamon
 * project: FavorMe
 */
public abstract class AbstractActivity<Controllable extends AbstractFragment & StateControllable> extends Activity implements TaggedView {

    public static final    int                 RESULT_EDITED      = 0xedf;
    public static final    int                 RESULT_DELETED     = 0xdef;
    public static final    String              CUSTOM_DATA        = ".custom:key_data";
    protected static final Stack<AsyncRequest> POSTPONED_CALLS    = new Stack<>();
    public static final    BroadcastFilter     DEFAULT_FILTERS    = new BroadcastFilter();
    protected final        BroadcastFilter     FILTER             = new BroadcastFilter();
    protected final        List<OnServerReady> mServerListeners   = new ArrayList<>();
    private final          String              TAG                = ((Object) this).getClass().getSimpleName();
    private final          Object              MUTEX              = new Object();
    private final          BroadcastReceiver   mErrorsReceiver    = new BroadcastReceiver() {

        @Implement
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;

            final ErrorReport report = intent.getParcelableExtra(Channel.KEY_DATA);
            if (NetworkNotification.FORCE_LOGOUT.isEqual(action)) {
                if (sfOnForceLogoutScreen != null) switchWorkflow(sfOnForceLogoutScreen);
            } else if (NetworkNotification.ALERT_NO_INTERNET.isEqual(action)) {
                openPopup(NoInternetPopup.class, getString(R.string.no_internet_access));
                informError(null);
            } else if (NetworkNotification.ALERT_EXCEPTION.isEqual(action)) {
                informError(report);
            } else if (NetworkNotification.INTERNET_ACCESS_GRANTED.isEqual(action)) {
                untwistStack();
            }
        }
    };
    private final          BroadcastReceiver   mMainAsyncReceiver = new BroadcastReceiver() {

        @Implement
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                assert action != null;

                if (mActivityListener != null) {
                    mActivityListener.onReceive(action, intent);
                }
            } catch (Exception any) {
                mActivityListener.onException(any);
            }
        }
    };
    private static Class<? extends AbstractActivity>    sfOnForceLogoutScreen;
    private static Class<? extends AbstractPopup>       sfOnServerOfflineScreen;
    protected      AnodaApplicationDelegate             mAppDelegate;
    protected      Class<? extends AbstractAsyncServer> mAsyncServiceImpl;
    protected      AbstractActivity                     mSelf;
    protected      RendezvousBinder                     mServerBinder;
    protected      AsyncReceiver                        mActivityListener;
    protected      LoaderCallbacks<Cursor>              mLoaderCallbacks;
    protected      ActionBarFactory                     mActionBar;
    protected      ActionBar                            mActionBarImpl;
    protected      int                                  mLoader;
    protected volatile boolean           mIsBound         = false;
    protected volatile boolean           isPaused         = true;
    private volatile   Controllable      mCurrentFragment = null;
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

    static {
        DEFAULT_FILTERS.addAction(NetworkNotification.FORCE_LOGOUT);
        DEFAULT_FILTERS.addAction(NetworkNotification.ALERT_EXCEPTION);
        DEFAULT_FILTERS.addAction(NetworkNotification.ALERT_NO_INTERNET);
        DEFAULT_FILTERS.addAction(NetworkNotification.INTERNET_ACCESS_GRANTED);
    }

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
    public final static void setDefaultOnServerOfflineScreen(Class<? extends AbstractPopup> klass) throws ClassNotFoundException, IllegalAccessException {
        if (klass == null)
            throw new ClassNotFoundException("Cannot find requested class declaration");
        if (sfOnServerOfflineScreen != null)
            throw new IllegalAccessException("Cannot assign new value to a final variable");

        sfOnServerOfflineScreen = klass;
    }

    public final AnodaApplicationDelegate getAppDelegate() {
        return mAppDelegate;
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
        LoaderManager manager = getLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        } else {
            return manager.initLoader(id, params, mLoaderCallbacks);
        }
    }

    protected Loader<Cursor> restartLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        LoaderManager manager = getLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        }

        return null;
    }

    protected void doBindService(Class<? extends AbstractAsyncServer> service) {
        mAsyncServiceImpl = service;
        bindService(new Intent(mSelf, service), mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinding randevu binder always processing in abstract instance
     */
    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mConnection.onServiceDisconnected(getCallingActivity());
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

        LayoutParams params = getWindow().getAttributes();
        params.height = metrics[0];
        params.width = metrics[1];
        params.alpha = 1f;
        params.dimAmount = .7f;
        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.y = isTablet ? marginTop << 2 : marginTop << 1;

        getWindow().setFlags(LayoutParams.FLAG_DIM_BEHIND, LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(params);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSelf = this;
        mAppDelegate = (AnodaApplicationDelegate) getApplication();

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        try {
            mActionBar = new ActionBarFactory(getActionBar());
        } catch (IllegalAccessException e) {
            logError(e);
        }
    }

    @Override
    protected void onResume() {
        if (mLoaderCallbacks != null) {
            initLoader(mLoader, null, mLoaderCallbacks);
        }

        super.onResume();

        isPaused = false;

        registerReceiver(mMainAsyncReceiver, FILTER);
        registerReceiver(mErrorsReceiver, DEFAULT_FILTERS);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mMainAsyncReceiver);
        unregisterReceiver(mErrorsReceiver);
        doUnbindService();

        isPaused = true;

        super.onPause();
    }

    protected void setContentView(int layout, AssetFont typeface) {
        View v = getLayoutInflater().inflate(layout, null);
        setContentView(v);

        FontsHelper.applyFonts(v, FontsHelper.getCustomFont(this, typeface));
    }

    protected void setContentView(View layout, AssetFont typeface) {
        setContentView(layout);

        FontsHelper.applyFonts(layout, FontsHelper.getCustomFont(this, typeface));
    }

    public final void registerAsyncReceiver(AsyncReceiver impl) {
        mActivityListener = impl;
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
        FragmentManager manager = getFragmentManager();
        if (!manager.popBackStackImmediate()) {
            if (mCurrentFragment != null) {
                syncState(mCurrentFragment.getState());
            }
            exitActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppDelegate.unregisterContext();

        FlurryAgent.onEndSession(mSelf);
    }

    public void startService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        startService(intent);
    }

    public void stopService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        stopService(intent);
    }

    public void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        intent.setAction(command.getAction());
        startService(intent);
    }

    public <T extends Parcelable> void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service, T data) {
        Intent intent = new Intent(this, service);
        intent.setAction(command.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        startService(intent);
    }

    public <T extends Parcelable> void startServiceWithAction(Broadcastable command, Class<? extends AbstractService> service, ArrayList<T> data) {
        Intent intent = new Intent(this, service);
        intent.setAction(command.getAction());
        intent.putExtra(Broadcastable.KEY_DATA, data);
        startService(intent);
    }

    public void sendOrderedBroadcastWithCallback(Broadcastable command, Permission permission, Broadcastable callbackCommand) {
        sendOrderedBroadcast(new Intent(command.getAction()).putExtra(CUSTOM_DATA, callbackCommand.getAction()), permission.getPermission());
    }

    /* universal broadcaster */
    public void sendBroadcast(Broadcastable command) {
        sendBroadcast(command, (Parcelable) null);
    }

    public void sendBroadcast(Broadcastable command, String data) {
        Bundle params = new Bundle();
        params.putString(CUSTOM_DATA, data);
        sendBroadcast(command, params);
    }

    public <Model extends Parcelable> void sendBroadcast(Broadcastable command, Model data) {
        Intent intent = new Intent(command.getAction());
        intent.putExtra(CUSTOM_DATA, data);
        sendBroadcast(intent);
    }

    /* Helper to postpone REST tasks */
    public void postponeRequest(AsyncRequest request) {
        if (accessAllowed() && !isPaused && mIsBound) {
            startAsyncServer(request);
        } else {
            addCallToStack(request);
        }
    }

    /* Helper to hide soft input KeyboardView */
    public void hideSoftInput(final int view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(view).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void hideSoftInput(final View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /* Helper to show soft input KeyboardView */
    public void showSoftInput(View v) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    /* Helper to announce message to user */
    public void shoutToast(String msg) {
        if (WordUtils.isEmpty(msg)) return;

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
        FragmentManager manager = getFragmentManager();
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
    public AbstractPopup openPopup(Class<? extends AbstractPopup> dialogClass) {
        AbstractPopup dialog = AbstractPopup.newInstance(dialogClass, null);
        return openPopupInternal(dialog);
    }

    public AbstractPopup openPopup(Class<? extends AbstractPopup> dialogClass, Bundle params) {
        AbstractPopup dialog = AbstractPopup.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractPopup openPopup(Class<? extends AbstractPopup> dialogClass, Parcelable data) {
        final Bundle params = new Bundle();
        params.putParcelable(CUSTOM_DATA, data);

        AbstractPopup dialog = AbstractPopup.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractPopup openPopup(Class<? extends AbstractPopup> dialogClass, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractPopup.IEXTRA_MESSAGE, message);

        AbstractPopup dialog = AbstractPopup.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractPopup openPopup(Class<? extends AbstractPopup> dialogClass, String title, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractPopup.IEXTRA_TITLE, title);
        params.putString(AbstractPopup.IEXTRA_MESSAGE, message);

        AbstractPopup dialog = AbstractPopup.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    private <TagDialog extends AbstractPopup & TaggedView> TagDialog openPopupInternal(TagDialog popup) {
        final String tag = popup.getViewTag();
        FragmentManager manager = getFragmentManager();

        if (!popup.isShowing(tag)) {
            popup.show(manager, tag);
            setServerListener(popup);
        }

        return popup;
    }

    public void startActivity(Broadcastable action) {
        startActivity(new Intent(action.getAction()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void startDisorderedActivity(Broadcastable action) {
        startActivity(new Intent(action.getAction()).setPackage(getPackageName()));
    }

    public void startDisorderedActivity(Broadcastable action, Bundle params) {
        Intent activity = new Intent(action.getAction());
        activity.setPackage(getPackageName());
        activity.putExtras(params);
        startActivity(activity);
    }

    public void startActivity(Broadcastable action, Bundle params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(params);
        startActivity(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, Data params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(CUSTOM_DATA, params);
        startActivity(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, ArrayList<Data> params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putParcelableArrayListExtra(CUSTOM_DATA, params);
        startActivity(i);
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

    /* Switch activity with anim */
    public void enterActivity(Class c) {
        startActivity(c, null);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
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
        intent.putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void enterActivity(Class c, ArrayList<T> data) {
        Intent intent = new Intent(mSelf, c);
        intent.putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result with anim */
    public <T extends Parcelable> void enterActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, ArrayList<T> data) {
        Intent intent = new Intent(mSelf, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
    }

    /* Exit to concrete activity with anim */
    public void exitActivity(Class c) {
        if (c != null) {
            startActivity(c, null);
        }
        hideSoftInput(getWindow().getDecorView());
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Return to previous activity with anim */
    public void exitActivity() {
        hideSoftInput(getWindow().getDecorView());
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Transcend result to launcher-activity and finish with anim */
    public void deliverResult(int r, Intent i) {
        setResult(r, i);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
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
        startActivity(intent);
    }

    /* Helper to open new activity with anim */
    private void startWorkflow(Class c, Intent i) {
        Intent intent = i != null ? i : new Intent(mSelf, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
        if (!accessAllowed()) openPopup(NoInternetPopup.class, getString(R.string.no_internet_access));
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

    private boolean accessAllowed() {
        return State.svAccessState == State.ACCESS_GRANTED || State.svAccessState == State.ACCESS_UNKNOWN;
    }
}
