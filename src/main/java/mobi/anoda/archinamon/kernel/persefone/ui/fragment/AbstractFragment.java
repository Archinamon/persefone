package mobi.anoda.archinamon.kernel.persefone.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import java.lang.reflect.Field;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DbLoader;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DialogLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.FragmentSwitcher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.interfaces.IOptionsVisibilityListener;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbstractFragment extends Fragment implements TaggedView {

    public static final String          TAG            = AbstractFragment.class.getSimpleName();
    protected final     BroadcastFilter fActionsFilter = new BroadcastFilter();
    protected           Bundle          mFragmentData  = new Bundle();
    private volatile boolean                    isAsyncChained;
    // replication of mHasMenu Fragment's field
    private          boolean                    mHasMenuReplica;
    private          IOptionsVisibilityListener mOptionsVisibilityListener;

    private StableContext    mStableContext;
    private SoftKeyboard     mKeyboardManagerDelegate;
    private BroadcastBus     mBroadcastBusDelegate;
    private ActivityLauncher mUiActivityLauncher;
    private FragmentSwitcher mUiFragmentSwitcher;
    private DialogLauncher   mUiDialogLauncher;
    private UiAffectionChain mUiAsyncChainBinder;
    private DbLoader         mAsyncDbLoader;

    public static AbstractFragment newInstance(Class<? extends AbstractFragment> klass, Bundle params) {
        final StableContext stableContext = StableContext.Impl.obtain();
        AbstractFragment instance = null;
        try {
            instance = klass.newInstance();
            instance.mFragmentData = params;
            instance.mStableContext = stableContext;
            instance.mUiActivityLauncher = new ActivityLauncher(stableContext);
            instance.mUiActivityLauncher.setFragment(instance);
            instance.mUiFragmentSwitcher = new FragmentSwitcher(stableContext);
            instance.mUiFragmentSwitcher.setFragment(instance);
            instance.mUiDialogLauncher = new DialogLauncher(stableContext);
            instance.mUiDialogLauncher.setFragment(instance);
        } catch (Exception e) {
            LogHelper.println_error(TAG, e);
        }

        return instance;
    }

    public void listenFor(Broadcastable action) {
        fActionsFilter.addAction(action);
    }

    public void connectAsyncChainBinder() {
        this.isAsyncChained = true;
    }

    @Nullable
    protected final ActionBar getActionBarImpl() {
        if (mStableContext.isUiContextRegistered()) {
            AbstractActivity uiContext = mStableContext.obtainUiContext();
            return uiContext.getActionBarImpl();
        }

        return null;
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    public void setOptionVisibilityListener(IOptionsVisibilityListener listener) {
        mOptionsVisibilityListener = listener;
    }

    @Override
    public void onResume() {
        if (mAsyncDbLoader != null)
            mAsyncDbLoader.onResume();

        super.onResume();

        if (mBroadcastBusDelegate != null) {
            mBroadcastBusDelegate.registerNetworkEventsForCurrentUiContext();
            mBroadcastBusDelegate.register(fActionsFilter);
        }
    }

    @Override
    public void onPause() {
        if (isAsyncChained)
            mUiAsyncChainBinder.doUnbindService();
        if (mBroadcastBusDelegate != null) {
            mBroadcastBusDelegate.unregisterNetworkEventsForCurrentUiContext();
            mBroadcastBusDelegate.unregister();
        }

        super.onPause();
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        if (mHasMenuReplica != hasMenu)
            mHasMenuReplica = hasMenu;

        super.setHasOptionsMenu(hasMenu);
    }

    @Override
    public void setMenuVisibility(final boolean menuVisible) {
        boolean menuVisibility;
        boolean isAccesible;
        Field field;

        try {
            field = Fragment.class.getDeclaredField("mMenuVisible");

            isAccesible = field.isAccessible();
            if (!isAccesible) {
                field.setAccessible(true);
            }

            menuVisibility = field.getBoolean(this);
            if (menuVisibility != menuVisible) {
                if (mHasMenuReplica && isAdded() && !isHidden()) {
                    if (mOptionsVisibilityListener != null) {
                        mOptionsVisibilityListener.onMenuDisposed(menuVisible);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logError(e);
        } finally {
            super.setMenuVisibility(menuVisible);
        }
    }

    public void onIntentDelivered(Intent intent) {
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

    protected View findViewById(@IdRes int id) {
        View root = getView();

        if (root != null)
            return root.findViewById(id);

        return null;
    }
}
