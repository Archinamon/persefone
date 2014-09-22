package mobi.anoda.archinamon.kernel.persefone.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.actionbar.ActionBarFactory;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable.FragmentState;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DbLoader;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.MetricsHelper;

/**
 * author: Archinamon project: FavorMe
 */
public abstract class AbstractActivity<Controllable extends AbstractFragment & StateControllable> extends ActionBarActivity implements TaggedView {

    protected enum PopupType {

        SMALL,
        LARGE
    }

    private final String          TAG            = ((Object) this).getClass()
                                                                  .getSimpleName();
    private final BroadcastFilter fActionsFilter = new BroadcastFilter();
    private          StableContext    mStableContext;
    protected        SoftKeyboard     mKeyboardManagerDelegate;
    protected        BroadcastBus     mBroadcastBusDelegate;
    protected        UiAffectionChain mUiAsyncChainBinder;
    protected        ActivityLauncher mUiActivityLauncher;
    protected        DbLoader         mAsyncDbLoader;
    protected        ActionBarFactory mActionBar;
    protected        ActionBar        mActionBarImpl;
    private volatile boolean          isPaused;
    private volatile boolean          isAsyncChained;
    private volatile Controllable mCurrentFragment = null;

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
        mStableContext = StableContext.instantiate(this);

        super.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        try {
            mActionBar = new ActionBarFactory(super.getSupportActionBar());
        } catch (IllegalAccessException e) {
            logError(e);
        }

        this.mUiActivityLauncher = new ActivityLauncher(mStableContext);
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

    public DbLoader getAsyncDbLoader() {
        if (mAsyncDbLoader == null)
            mAsyncDbLoader = new DbLoader(mStableContext);

        return this.mAsyncDbLoader;
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
            mUiActivityLauncher.exitActivity();
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

    public String getTextFromView(@IdRes int viewId) {
        View v = super.findViewById(viewId);
        if (v instanceof TextView) {
            TextView view = (TextView) v;
            return view.getText().toString();
        } else {
            return v.toString();
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            }
            transaction.addToBackStack(tag);
        }

        transaction.replace(R.id.fragment_view, fragment, tag);
        transaction.commit();

        if (mCurrentFragment != null) {
            syncState(mCurrentFragment.getState());
        }
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

            if (isAsyncChained) {
                mUiAsyncChainBinder.setServerListener(popup);
            }
        }

        return popup;
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
}
