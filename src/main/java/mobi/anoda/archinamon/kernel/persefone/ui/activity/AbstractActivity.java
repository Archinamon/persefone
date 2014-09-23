package mobi.anoda.archinamon.kernel.persefone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.actionbar.ActionBarFactory;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DbLoader;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DialogLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.FragmentSwitcher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.MetricsHelper;

/**
 * author: Archinamon project: FavorMe
 */
public abstract class AbstractActivity extends ActionBarActivity {

    protected enum PopupType {

        SMALL,
        LARGE
    }

    private final String          TAG            = Common.obtainClassTag(this);
    private final BroadcastFilter fActionsFilter = new BroadcastFilter();
    private          StableContext    mStableContext;
    protected        SoftKeyboard     mKeyboardManagerDelegate;
    protected        BroadcastBus     mBroadcastBusDelegate;
    protected        UiAffectionChain mUiAsyncChainBinder;
    protected        ActivityLauncher mUiActivityLauncher;
    protected        FragmentSwitcher mUiFragmentSwitcher;
    protected        DialogLauncher   mUiDialogLauncher;
    protected        DbLoader         mAsyncDbLoader;
    protected        ActionBarFactory mActionBar;
    protected        ActionBar        mActionBarImpl;
    private volatile boolean          isPaused;
    private volatile boolean          isAsyncChained;

    public void listenFor(Broadcastable action) {
        fActionsFilter.addAction(action);
    }

    public void connectAsyncChainBinder() {
        this.isAsyncChained = true;
    }

    public final boolean isPaused() {
        return this.isPaused;
    }

    public final ActionBar getActionBarImpl() {
        return this.mActionBarImpl;
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

        getWindow().setAttributes(params);
        getWindow().setFlags(LayoutParams.FLAG_DIM_BEHIND, LayoutParams.FLAG_DIM_BEHIND);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initHandlers();
    }

    protected void onCreateWithActionBar(Bundle savedInstanceState) {
        super.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        try {
            mActionBar = new ActionBarFactory(super.getSupportActionBar());
        } catch (IllegalAccessException e) {
            logError(e);
        }

        initHandlers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAsyncDbLoader != null)
            mAsyncDbLoader.onResume();

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

        if (mBroadcastBusDelegate != null) {
            mBroadcastBusDelegate.registerNetworkEventsForCurrentUiContext();
            mBroadcastBusDelegate.register(fActionsFilter);
        }
    }

    @Override
    protected void onStop() {
        if (isAsyncChained) mUiAsyncChainBinder.doUnbindService();
        if (mBroadcastBusDelegate != null) {
            mBroadcastBusDelegate.unregisterNetworkEventsForCurrentUiContext();
            mBroadcastBusDelegate.unregister();
        }

        super.onStop();
    }

    public UiAffectionChain getUiAsyncChainBinder() {
        if (mUiAsyncChainBinder == null) {
            mUiAsyncChainBinder = new UiAffectionChain(mStableContext);
        }

        return this.mUiAsyncChainBinder;
    }

    public SoftKeyboard getKeyboardDelegate() {
        if (mKeyboardManagerDelegate == null) {
            mKeyboardManagerDelegate = new SoftKeyboard(mStableContext);
        }

        return this.mKeyboardManagerDelegate;
    }

    public BroadcastBus getBroadcastBusDelegate() {
        if (mBroadcastBusDelegate == null) {
            mBroadcastBusDelegate = new BroadcastBus(mStableContext);
        }

        return this.mBroadcastBusDelegate;
    }

    public ActivityLauncher getUiActivityLauncher() {
        return this.mUiActivityLauncher;
    }

    public FragmentSwitcher getUiFragmentSwitcher() {
        return this.mUiFragmentSwitcher;
    }

    public DialogLauncher getUiDialogLauncher() {
        return this.mUiDialogLauncher;
    }

    public DbLoader getAsyncDbLoader() {
        if (mAsyncDbLoader == null)
            mAsyncDbLoader = new DbLoader(mStableContext);

        return this.mAsyncDbLoader;
    }

    public void startService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        super.startService(intent);
    }

    public void stopService(Class<? extends AbstractService> service) {
        Intent intent = new Intent(this, service);
        super.stopService(intent);
    }

    /* Generalized REST errors parser */
    protected String parseError(ErrorReport report) {
        String error = "";
        if (report != null) {
            error = report.getMessage();
        }

        return error;
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    private void initHandlers() {
        this.mStableContext = StableContext.Impl.instantiate(this);
        this.mUiActivityLauncher = new ActivityLauncher(mStableContext);
        this.mUiFragmentSwitcher = new FragmentSwitcher(mStableContext);
        this.mUiDialogLauncher = new DialogLauncher(mStableContext);
    }
}
