package mobi.anoda.archinamon.kernel.persefone.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Stack;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.State;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.signals.AsyncReceiver;
import mobi.anoda.archinamon.kernel.persefone.signals.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.OnServerReady;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable.FragmentState;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.NoInternetDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.interfaces.IOptionsVisibilityListener;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbstractFragment extends Fragment implements TaggedView, OnServerReady {

    public static final    String              TAG              = AbstractFragment.class.getSimpleName();
    public static final    String              CUSTOM_DATA      = AbstractActivity.CUSTOM_DATA;
    protected static final Stack<AsyncRequest> POSTPONED_CALLS  = new Stack<>();
    protected static final BroadcastFilter     DEFAULT_FILTER   = new BroadcastFilter();
    protected final        BroadcastFilter     FILTER           = new BroadcastFilter();
    private final          Object              MUTEX            = new Object();
    private final          BroadcastReceiver   mDefaultReceiver = new BroadcastReceiver() {

        @Implement
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        assert action != null;

        if (NetworkNotification.INTERNET_ACCESS_GRANTED.isEqual(action)) {
            untwistStack();
        }
        }
    };
    private final          BroadcastReceiver   mAsyncReceiver   = new BroadcastReceiver() {

        @Implement
        public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            assert action != null;

            if (mFragmentListener != null) {
                mFragmentListener.onReceive(action, intent);
            }
        } catch (Exception any) {
            mFragmentListener.onException(any);
        }
        }
    };
    protected              Bundle              mFragmentData    = new Bundle();
    protected          AsyncReceiver              mFragmentListener;
    protected          LoaderCallbacks<Cursor>    mLoaderCallbacks;
    protected          AnodaApplicationDelegate   mAppDelegate;
    protected          AbstractActivity           mContext;
    protected volatile boolean                    isServerBinded;
    protected volatile int                        mLoader;
    private volatile   FragmentState              mInnerState;
    // replication of mHasMenu Fragment's field
    private            boolean                    mHasMenuReplica;
    private            IOptionsVisibilityListener mOptionsVisibilityListener;

    static {
        DEFAULT_FILTER.addAction(NetworkNotification.INTERNET_ACCESS_GRANTED);
    }

    public static AbstractFragment newInstance(Class<? extends AbstractFragment> klass, Bundle params) {
        AbstractFragment instance = null;
        try {
            instance = klass.newInstance();
            instance.mFragmentData = params;
        } catch (Exception e) {
            LogHelper.println_error(TAG, e);
        }

        return instance;
    }

    @Nullable
    protected final ActionBar getActionBarImpl() {
        return mContext.getActionBarImpl();
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    public /*virtual*/ boolean informError(ErrorReport report) {
        return true;
    }

    public void tryToObtainContext() {
        if (mContext != null) {
            return;
        }

        mContext = (AbstractActivity) getActivity();
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

    @Implement
    public void onBind() {
        synchronized (MUTEX) {
            isServerBinded = true;

            if (accessAllowed())
                untwistStack();
        }
    }

    @Implement
    public void onDisconnect() {
        synchronized (MUTEX) {
            isServerBinded = false;
        }
    }

    @Implement
    public void onRendezvous(AsyncRequest request) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this instanceof AsyncReceiver) {
            registerAsyncReceiver((AsyncReceiver) this);
        }

        mAppDelegate = (AnodaApplicationDelegate) mContext.getApplication();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        syncState();
    }

    @Override
    public void onResume() {
        if (mLoaderCallbacks != null) {
            initLoader(mLoader, null, mLoaderCallbacks);
        }

        super.onResume();

        mContext.registerReceiver(mDefaultReceiver, DEFAULT_FILTER);
        if (mFragmentListener != null) {
            mContext.registerReceiver(mAsyncReceiver, FILTER);
        }
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mDefaultReceiver);
        if (mFragmentListener != null) {
            mContext.unregisterReceiver(mAsyncReceiver);
        }

        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof  AbstractActivity) {
            this.mContext = (AbstractActivity) activity;
            this.mContext.setServerListener(this);
        }
    }

    @Override
    public void onDetach() {
        this.mContext.removeServerListener(this);
        super.onDetach();
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

    protected View findViewById(int id) {
        View root = getView();

        return checkNotNull(root).findViewById(id);
    }

    public final void registerAsyncReceiver(AsyncReceiver impl) {
        mFragmentListener = impl;
    }

    /* Helper to postpone REST tasks */
    public void postponeRequest(AsyncRequest request) {
        if (isServerBinded && accessAllowed()) {
            startAsyncServer(request);
        } else {
            addCallToStack(request);
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

    /* Switch activity for result with anim */
    public <T extends Parcelable> void enterActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mContext, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
        mContext.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result with anim */
    public <T extends Parcelable> void enterActivityForResult(Class c, int code, ArrayList<T> data) {
        Intent intent = new Intent(mContext, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
        mContext.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    public <T extends Parcelable> void openActivityForResult(Class c, int code, ArrayList<T> data) {
        Intent intent = new Intent(mContext, c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivityForResult(intent, code);
    }

    public <T extends Parcelable> void openActivityForResult(Class c, int code) {
        Intent intent = new Intent(mContext, c);

        startActivityForResult(intent, code);
    }

    protected Loader<Cursor> initLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        if (isDetached()) {
            return null;
        }

        LoaderManager manager = getLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        } else {
            return manager.initLoader(id, params, mLoaderCallbacks);
        }
    }

    protected Loader<Cursor> restartLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        if (isDetached()) {
            return null;
        }

        LoaderManager manager = getLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        }

        return null;
    }

    /* Helper for untwisting postponed REST tasks */
    private void untwistStack() {
        if (!POSTPONED_CALLS.empty()) {
            synchronized (MUTEX) {
                while (!POSTPONED_CALLS.empty()) {
                    AsyncRequest request = POSTPONED_CALLS.pop();
                    AsyncRequest.send(mContext, request, mContext.getServerImpl());
                    onRendezvous(request);
                }
            }
        }
    }

    private void startAsyncServer(AsyncRequest request) {
        AsyncRequest.send(mContext, request, mContext.getServerImpl());
        onRendezvous(request);
    }

    private void addCallToStack(AsyncRequest request) {
        if (!accessAllowed()) mContext.openPopup(NoInternetDialog.class, getString(R.string.no_internet_access));
        synchronized (MUTEX) {
            POSTPONED_CALLS.push(request);
        }
    }

    private boolean accessAllowed() {
        return State.svAccessState == State.ACCESS_GRANTED || State.svAccessState == State.ACCESS_UNKNOWN;
    }
}
