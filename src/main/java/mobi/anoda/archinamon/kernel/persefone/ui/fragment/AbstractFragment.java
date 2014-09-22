package mobi.anoda.archinamon.kernel.persefone.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.lang.reflect.Field;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable.FragmentState;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DbLoader;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
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
    private volatile FragmentState              mInnerState;
    // replication of mHasMenu Fragment's field
    private          boolean                    mHasMenuReplica;
    private          IOptionsVisibilityListener mOptionsVisibilityListener;

    private   StableContext    mStableContext;
    protected SoftKeyboard     mKeyboardManagerDelegate;
    protected BroadcastBus     mBroadcastBusDelegate;
    private   ActivityLauncher mUiActivityLauncher;
    protected UiAffectionChain mUiAsyncChainBinder;
    protected DbLoader         mAsyncDbLoader;

    public static AbstractFragment newInstance(Class<? extends AbstractFragment> klass, Bundle params) {
        final StableContext stableContext = StableContext.obtain();
        AbstractFragment instance = null;
        try {
            instance = klass.newInstance();
            instance.mFragmentData = params;
            instance.mStableContext = stableContext;
            instance.mUiActivityLauncher = new ActivityLauncher(stableContext);
            instance.mUiActivityLauncher.setFragment(instance);
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

    public final synchronized void syncState() {
        if (mInnerState == null) {
            mInnerState = FragmentState.ATTACHED;
            return;
        }

        switch (mInnerState) {
            case DETACHED:
                mInnerState = FragmentState.ATTACHED;
                break;
            case ATTACHED:
                mInnerState = FragmentState.DETACHED;
                break;
        }

        Log.w(TAG, mInnerState.name());
    }

    public void setOptionVisibilityListener(IOptionsVisibilityListener listener) {
        mOptionsVisibilityListener = listener;
    }

    @Nullable
    protected final FragmentState getInnerState() {
        return mInnerState;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        syncState();
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
        boolean menuVisibility = false;//default value
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

    public String getTextFromView(@IdRes int viewId) {
        View v = findViewById(viewId);
        if (v instanceof TextView) {
            TextView view = (TextView) v;
            return view.getText()
                       .toString();
        } else {
            return v.toString();
        }
    }

    /* Switch fragment inside nested fragment */
    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, int resId, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, null);

        switchFragmentInternal(fragment, resId, addToStack);

        return fragment;
    }

    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, int resId, Bundle params, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, resId, addToStack);

        return fragment;
    }

    public <D extends Parcelable> AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, int resId, D data, boolean addToStack) {
        Bundle params = new Bundle();
        params.putParcelable(CUSTOM_DATA, data);
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, resId, addToStack);

        return fragment;
    }

    private <TagFrmt extends AbstractFragment & TaggedView> void switchFragmentInternal(TagFrmt fragment, int resId, boolean isAddingToBackstack) {
        final String tag = fragment.getViewTag();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (isAddingToBackstack) {
            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            transaction.addToBackStack(tag);
        }

        transaction.replace(resId, fragment, tag);
        transaction.commit();
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
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            popup.show(manager, tag);
        }

        return popup;
    }
}
