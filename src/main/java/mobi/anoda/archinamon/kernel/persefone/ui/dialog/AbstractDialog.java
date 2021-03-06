package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import org.intellij.lang.annotations.MagicConstant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.BroadcastFilter;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.adapter.AbstractAdapter;
import mobi.anoda.archinamon.kernel.persefone.ui.async.binder.UiAffectionChain;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.ActivityLauncher;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.BroadcastBus;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.DbLoader;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.SoftKeyboard;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * author: Archinamon project: FavorMe
 */
public abstract class AbstractDialog extends DialogFragment implements TaggedView {

    public static interface ICustomInjection {

        void makeInjection(@Nullable final View body) throws IllegalAccessException;
    }

    protected enum Popup {

        ALERT,
        PROGRESS_SIMPLE,
        PROGRESS_CANCELABLE,
        LIST_SELECTOR,
        LIST_MULTICHECK,
        PROMPT_YES_NO,
        PROMPT_EXTENDED,
        CUSTOM
    }

    protected static enum Theme {

        LIGHT(android.R.style.Theme_Holo_Light_Dialog_NoActionBar),
        DARK(android.R.style.Theme_Holo_Dialog_NoActionBar);

        private final int mThemeId;

        private Theme(int themeId) {
            mThemeId = themeId;
        }

        public int theme() {
            return mThemeId;
        }
    }

    protected static final class ListElem {

        private Integer mId;
        private String  mStrData;

        public ListElem() {
        }

        public Integer getId() {
            return mId;
        }

        public void setId(int id) {
            mId = id;
        }

        public void setString(String str) {
            mStrData = str;
        }

        @Override
        public String toString() {
            return mStrData;
        }
    }

    protected final class ShadowAdapter extends AbstractAdapter<ListElem> {

        public ShadowAdapter(int resource, int textViewResourceId, List<ListElem> objects) {
            super(resource, textViewResourceId, objects);

            setNotifyOnChange(true);
        }

        @Implement
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = createViewFromResource(position, convertView, parent, mItemResource);
            applyFonts(convertView);

            return convertView;
        }
    }

    public static final              String               TAG             = AbstractDialog.class.getSimpleName();
    public static final              String               IEXTRA_TITLE    = ":popup_title";
    public static final              String               IEXTRA_MESSAGE  = ":popup_message";
    public static final              String               IEXTRA_THEME    = ":popup_theme";
    protected final                  BroadcastFilter      fActionsFilter  = new BroadcastFilter();
    protected static transient final Map<String, Boolean> svWorkingPopups = new Hashtable<>();
    // Sys mapping
    protected                  Bundle                   mDialogParams;
    protected                  ContextThemeWrapper      mThemeWrapper;
    protected                  AlertDialog.Builder      mBuilder;
    protected                  LayoutInflater           mInflater;
    private volatile transient String                   mCurrentTag;
    // Common dialog data
    private                    Dialog                   mDialog;
    private                    Popup                    mDialogType;
    private                    String                   mDialogTitle;
    private                    String                   mDialogMessage;
    private                    String[]                 mDialogListOptions;
    private                    ShadowAdapter            mListAdapter;
    // Callbacks
    private                    OnItemClickListener      mOnListItemClickCallback;
    private                    OnClickListener          mOnClickOkCallback;
    private                    OnClickListener          mOnClickCancelCallback;
    private                    OnClickListener          mOnClickNeutralCallback;
    // Customization
    private                    int                      mDialogTheme;
    private                    int                      mProgressStyle;
    private                    String                   mButtonOkTitle;
    private                    String                   mButtonCancelTitle;
    private                    String                   mButtonNeutralTitle;
    private                    ICustomInjection         mCustomModelInjector;
    private                    View                     mCustomLayout;
    private                    View                     mCustomTitleLayout;
    private int     mDialogThemeDark  = android.R.style.Theme_Holo_DialogWhenLarge;
    private int     mDialogThemeLight = android.R.style.Theme_Holo_Light_DialogWhenLarge;
    private boolean mIsReady          = false;
    private volatile boolean isAsyncChained;

    private StableContext    mStableContext;
    private SoftKeyboard     mKeyboardManagerDelegate;
    private BroadcastBus     mBroadcastBusDelegate;
    private ActivityLauncher mUiActivityLauncher;
    private UiAffectionChain mUiAsyncChainBinder;
    private DbLoader         mAsyncDbLoader;

    public static AbstractDialog newInstance(Class<? extends AbstractDialog> klass, Bundle params) {
        final StableContext stableContext = StableContext.Impl.obtain();
        AbstractDialog instance = null;
        try {
            instance = klass.newInstance();
            instance.mDialogParams = params;
            instance.mStableContext = stableContext;
            instance.mUiActivityLauncher = new ActivityLauncher(stableContext);
            instance.mUiActivityLauncher.setDialog(instance);
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

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    public final boolean isShowing(String tag) {
        return svWorkingPopups.containsKey(tag) && svWorkingPopups.get(tag);
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        mCurrentTag = tag;
        super.show(manager, tag);
        svWorkingPopups.put(tag, true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        svWorkingPopups.put(mCurrentTag, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInflater = (LayoutInflater) mStableContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mDialogParams != null) {
            if (mDialogParams.containsKey(IEXTRA_THEME))
                setTheme(mDialogParams.getInt(IEXTRA_THEME));
            if (mDialogParams.containsKey(IEXTRA_MESSAGE))
                setMessage(mDialogParams.getString(IEXTRA_MESSAGE));
            if (mDialogParams.containsKey(IEXTRA_TITLE))
                setTitle(mDialogParams.getString(IEXTRA_TITLE));
        }

        mIsReady = setup();
        mThemeWrapper = new ContextThemeWrapper(mStableContext.obtainAppContext(), mDialogTheme);
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mIsReady) {
            setupDialog();
            return mDialog;
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyFonts(view);
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

    // CUSTOMIZERS

    protected abstract boolean setup();

    protected void applyFonts(View view) {
    }

    protected final ShadowAdapter getAdapter() {
        return mListAdapter;
    }

    protected final void setDialogType(int num) {
        Popup[] types = Popup.values();
        if (num < types.length) {
            mDialogType = types[num];
        }
    }

    protected final void setDialogType(Popup type) {
        mDialogType = type;
    }

    protected final void setProgressStyle(@MagicConstant(flagsFromClass = ProgressDialog.class) int style) {
        mProgressStyle = style;
    }

    protected final void setTheme(int style) {
        mDialogTheme = style;
    }

    protected final void setTheme(Theme style) {
        mDialogTheme = style.theme();
    }

    protected final void setTitle(int resId) {
        mDialogTitle = getString(resId);
    }

    protected final void setTitle(String title) {
        mDialogTitle = title;
    }

    protected final void setMessage(int resId) {
        mDialogMessage = getString(resId);
    }

    protected final void setMessage(String title) {
        mDialogMessage = title;
    }

    protected final void setListOptions(int resId) {
        mDialogListOptions = getResources().getStringArray(resId);
    }

    protected final void setListOptions(String[] options) {
        mDialogListOptions = options;
    }

    protected final void setListItemCallback(OnItemClickListener listener) {
        mOnListItemClickCallback = listener;
    }

    protected final void setOkButton(@Nullable Integer resId, OnClickListener callback) {
        mButtonOkTitle = getString(resId != null ? resId : android.R.string.ok);
        mOnClickOkCallback = callback;
    }

    protected final void setOkButton(@Nullable String title, OnClickListener callback) {
        mButtonOkTitle = title != null ? title : getString(android.R.string.ok);
        mOnClickOkCallback = callback;
    }

    protected final void setCancelButton(@Nullable Integer resId, OnClickListener callback) {
        mButtonCancelTitle = getString(resId != null ? resId : android.R.string.no);
        mOnClickCancelCallback = callback;
    }

    protected final void setCancelButton(@Nullable String title, OnClickListener callback) {
        mButtonCancelTitle = title != null ? title : getString(android.R.string.no);
        mOnClickCancelCallback = callback;
    }

    protected final void setNeutralButton(@Nullable Integer resId, OnClickListener callback) {
        mButtonNeutralTitle = getString(resId != null ? resId : android.R.string.cancel);
        mOnClickNeutralCallback = callback;
    }

    protected final void setNeutralButton(@Nullable String title, OnClickListener callback) {
        mButtonNeutralTitle = title != null ? title : getString(android.R.string.cancel);
        mOnClickNeutralCallback = callback;
    }

    protected final void setCustomizer(ICustomInjection injector) {
        mCustomModelInjector = injector;
    }

    protected final void setCustomLayoutId(int redId) {
        mCustomLayout = mInflater.inflate(redId, null);
    }

    protected final void setCustomView(View v) {
        mCustomLayout = v;
    }

    protected final void setCustomTitleLayoutId(int resId) {
        mCustomTitleLayout = mInflater.inflate(resId, null);
    }

    protected final void setCustomTitleLayout(View v) {
        mCustomTitleLayout = v;
    }

    // PRIVATE BUILDERS

    private void setupDialog() {
        switch (mDialogType) {
            case ALERT:
                buildAlert();
                break;
            case PROGRESS_SIMPLE:
                buildProgress();
                setCancelable(false);
                break;
            case PROGRESS_CANCELABLE:
                buildProgress();
                setCancelable(true);
                break;
            case LIST_SELECTOR:
                ListView v = buildList();
                v.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;
            case LIST_MULTICHECK:
                ListView lv = buildList();
                lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;
            case PROMPT_YES_NO:
                buildPrompt();
                break;
            case PROMPT_EXTENDED:
                buildPromptExt();
                break;
            case CUSTOM:
                buildCustom();
                break;
            default:
                throw new IllegalStateException("You need to specify dialog type to point internal builder");
        }

        //injection of customDialog code
        if (mCustomModelInjector != null) {
            try {
                mCustomModelInjector.makeInjection(mCustomLayout);
            } catch (Exception accessExp) {
                accessExp.printStackTrace();//print error and silently pass next
            }
        }

        if (mDialog == null && mBuilder != null) {
            mDialog = mBuilder.create();
        }
    }

    private void buildAlert() {
        mBuilder = new AlertDialog.Builder(mThemeWrapper).setTitle(mDialogTitle)
                                                         .setMessage(mDialogMessage)
                                                         .setPositiveButton(mButtonOkTitle, mOnClickOkCallback);

        mBuilder.setCancelable(false);
        setCancelable(false);
    }

    private void buildProgress() {
        final int style = mDialogTheme == mDialogThemeLight ? R.style.ProgressPopupLight : R.style.ProgressPopupDark;
        mDialog = new android.app.ProgressDialog(mStableContext.obtainAppContext(), style);
        ((android.app.ProgressDialog) mDialog).setMessage(mDialogMessage);
        ((android.app.ProgressDialog) mDialog).setProgressStyle(mProgressStyle);
    }

    private ListView buildList() {
        View view = mInflater.inflate(R.layout.popup_list, null);
        assert view != null;

        mListAdapter = new ShadowAdapter( R.layout.item_popup_list, android.R.id.text1, buildListOptions());

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(mOnListItemClickCallback);

        mBuilder = new AlertDialog.Builder(mThemeWrapper).setView(view)
                                                         .setTitle(mDialogTitle);

        mBuilder.setCancelable(true);
        setCancelable(true);

        return listView;
    }

    private void buildPrompt() {
        mBuilder = new AlertDialog.Builder(mThemeWrapper).setTitle(mDialogTitle)
                                                         .setMessage(mDialogMessage)
                                                         .setPositiveButton(mButtonOkTitle, mOnClickOkCallback)
                                                         .setNegativeButton(mButtonCancelTitle, mOnClickCancelCallback);
    }

    private void buildPromptExt() {
        buildPrompt();
        mBuilder.setNeutralButton(mButtonNeutralTitle, mOnClickNeutralCallback);
    }

    /**
     * now we can modify options list via ShadowAdapter instance and get real disposition according to initial list by getItem adapter's method
     */
    private ArrayList<ListElem> buildListOptions() {
        ArrayList<ListElem> options = new ArrayList<>();

        if (mDialogListOptions != null) {
            final int size = mDialogListOptions.length;
            for (int i = 0; i < size; i++) {
                ListElem item = new ListElem();
                item.setId(i);
                item.setString(mDialogListOptions[i]);

                options.add(i, item);
            }
        }

        return options;
    }

    private void buildCustom() {
        Context context = mStableContext.obtainAppContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mBuilder = new AlertDialog.Builder(context, mDialogTheme);
        } else {
            mBuilder = new AlertDialog.Builder(context);
            mBuilder.setInverseBackgroundForced(true);
        }

        if (mDialogTitle != null) {
            mBuilder.setTitle(mDialogTitle);
        }

        if (mDialogMessage != null) {
            mBuilder.setMessage(mDialogMessage);
        }

        if (mCustomLayout != null) {
            mBuilder.setView(mCustomLayout);
        }

        if (mCustomTitleLayout != null) {
            mBuilder.setCustomTitle(mCustomTitleLayout);
        }

        if (mButtonOkTitle != null || mOnClickOkCallback != null) {
            mBuilder.setPositiveButton(mButtonOkTitle, mOnClickOkCallback);
        }

        if (mButtonCancelTitle != null || mOnClickCancelCallback != null) {
            mBuilder.setNegativeButton(mButtonCancelTitle, mOnClickCancelCallback);
        }

        if (mButtonNeutralTitle != null || mOnClickNeutralCallback != null) {
            mBuilder.setNeutralButton(mButtonNeutralTitle, mOnClickNeutralCallback);
        }
    }
}
