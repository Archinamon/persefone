package mobi.anoda.archinamon.kernel.persefone.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.settings.MobiSettings;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

import static com.google.common.base.Preconditions.checkNotNull;
import static mobi.anoda.archinamon.kernel.persefone.settings.MobiSettings.getInstance;

public abstract class AbsPreferenceFragment<Settings extends MobiSettings> extends AbsListFragment implements TaggedView, OnSharedPreferenceChangeListener {

    public static final  String TAG                  = AbsPreferenceFragment.class.getSimpleName();
    private static final long   POST_DELAY           = 500L;
    private static final int    FIRST_REQUEST_CODE   = 100;
    private static final int    MSG_BIND_PREFERENCES = 1;
    private   Handler                  mHandler;
    private   boolean                  mHavePrefs;
    private   boolean                  mInitDone;
    private   ListView                 mList;
    private   String                   mPreferenceTag;
    private   String                   mPreferenceName;
    private   int                      mResourceId;
    protected AnodaApplicationDelegate mAppDelegate;
    protected AbstractActivity         mContext;
    @Nullable
    protected Settings                 mSettings;
    protected SharedPreferences        mPreferences;
    protected PreferenceManager        mPreferenceManager;
    private final Runnable mRequestFocus = new Runnable() {

        @Implement
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    /*dynamic*/ {
        onInit();
    }

    public AbsPreferenceFragment() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_BIND_PREFERENCES:
                        bindPreferences();
                        break;
                }
            }
        };
    }

    protected void setPreferenceTag(String name) {
        this.mPreferenceTag = name;
    }

    protected void setPreferenceName(String name) {
        this.mPreferenceName = name;
    }

    protected void setPreferencesResource(int id) {
        this.mResourceId = id;
    }

    protected abstract void onInit();

    protected void onFinishPreferencesInflate(@Nullable View view) {
    }

    protected void onFinishCreate() {
    }

    protected AbstractActivity getContext() {
        return this.mContext;
    }

    protected Settings getSettings() {
        return mSettings;
    }

    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(inflateFromIntent(intent, getPreferenceScreen()));
    }

    public void addPreferencesFromResource(int i) {
        requirePreferenceManager();
        setPreferenceScreen(inflateFromResource(mContext, i, getPreferenceScreen()));
    }

    public Preference findPreference(CharSequence charsequence) {
        if (mPreferenceManager == null) {
            return null;
        } else {
            return mPreferenceManager.findPreference(charsequence);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public PreferenceScreen getPreferenceScreen() {
        PreferenceScreen preferencescreen;
        try {
            Method method = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            method.setAccessible(true);
            preferencescreen = (PreferenceScreen) method.invoke(mPreferenceManager);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return preferencescreen;
    }

    public PreferenceScreen inflateFromIntent(Intent intent, PreferenceScreen preferencescreen) {
        PreferenceScreen preferencescreen1;
        try {
            Method method = PreferenceManager.class.getDeclaredMethod("inflateFromIntent", Intent.class, PreferenceScreen.class);
            method.setAccessible(true);
            preferencescreen1 = (PreferenceScreen) method.invoke(mPreferenceManager, intent, preferencescreen);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return preferencescreen1;
    }

    public PreferenceScreen inflateFromResource(Context context, int resourceId, PreferenceScreen preferencescreen) {
        PreferenceScreen preferencescreen1;
        try {
            Class aclass[] = new Class[] {Context.class, Integer.TYPE, PreferenceScreen.class};

            Method method = PreferenceManager.class.getDeclaredMethod("inflateFromResource", aclass);
            method.setAccessible(true);

            Object aobj[] = new Object[] {context, resourceId, preferencescreen};
            preferencescreen1 = (PreferenceScreen) method.invoke(mPreferenceManager, aobj);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return preferencescreen1;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AbstractActivity) {
            mContext = (AbstractActivity) activity;
            checkNotNull(mContext);

            mAppDelegate = mContext.getAppDelegate();
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        if (mHavePrefs) {
            bindPreferences();
        }

        mInitDone = true;
        if (bundle != null) {
            Bundle bundle1 = bundle.getBundle(mPreferenceTag);
            if (bundle1 != null) {
                PreferenceScreen preferencescreen = getPreferenceScreen();
                if (preferencescreen != null) {
                    preferencescreen.restoreHierarchyState(bundle1);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        dispatchActivityResult(requestCode, resultCode, intent);
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListLayout(R.layout.preference_list_content);

        mPreferenceManager = createPreferenceManager();
        mPreferenceManager.setSharedPreferencesName(mPreferenceName);
        addPreferencesFromResource(mResourceId);

        mPreferences = mPreferenceManager.getSharedPreferences();

        ParameterizedType generic = (ParameterizedType) ((Object) this).getClass().getGenericSuperclass();
        Type[] arguments = generic.getActualTypeArguments();
        if (arguments != null && arguments.length > 0) {
            Class<Settings> type = (Class<Settings>) arguments[0];

            mSettings = getInstance(mContext, mPreferenceTag, type);
        }

        onFinishCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        dispatchActivityStop();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        PreferenceScreen preferencescreen = getPreferenceScreen();
        if (preferencescreen != null) {
            Bundle bundle1 = new Bundle();
            preferencescreen.saveHierarchyState(bundle1);
            bundle.putBundle(mPreferenceTag, bundle1);
        }
    }

    @Override
    public void onDestroyView() {
        mList = null;
        mHandler.removeCallbacks(mRequestFocus);
        mHandler.removeMessages(1);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatchActivityDestroy();
    }

    @Implement
    protected final void onListItemClick(AdapterView parentView, View view, int position, long id) {}

    private PreferenceManager createPreferenceManager() {
        PreferenceManager preferencemanager;
        try {
            Class aclass[] = new Class[] {Activity.class, Integer.TYPE};
            Constructor constructor = PreferenceManager.class.getDeclaredConstructor(aclass);
            constructor.setAccessible(true);

            Object aobj[] = new Object[] {mContext, FIRST_REQUEST_CODE};
            preferencemanager = (PreferenceManager) constructor.newInstance(aobj);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return preferencemanager;
    }

    private void dispatchActivityDestroy() {
        try {
            Method method = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            method.setAccessible(true);
            method.invoke(mPreferenceManager);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void dispatchActivityResult(int resultCode, int requestCode, Intent intent) {
        try {
            Class aclass[] = new Class[] {Integer.TYPE, Integer.TYPE, Intent.class};
            Method method = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", aclass);
            method.setAccessible(true);

            PreferenceManager preferencemanager = mPreferenceManager;
            Object aobj[] = new Object[] {resultCode, requestCode, intent};
            method.invoke(preferencemanager, aobj);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void dispatchActivityStop() {
        try {
            Method method = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            method.setAccessible(true);
            method.invoke(mPreferenceManager);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void postBindPreferences() {
        if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) {
            mHandler.obtainMessage(MSG_BIND_PREFERENCES)
                    .sendToTarget();
        }
    }

    private void requirePreferenceManager() {
        if (mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void setFragment(PreferenceFragment preferencefragment) {
        try {
            Method method = PreferenceManager.class.getDeclaredMethod("setFragment", PreferenceFragment.class);
            method.setAccessible(true);
            method.invoke(mPreferenceManager, preferencefragment);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setPreferenceScreen(PreferenceScreen preferencescreen) {
        Method method;
        try {
            method = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            method.setAccessible(true);

            if (!(Boolean) method.invoke(mPreferenceManager, preferencescreen) || preferencescreen == null) {
                throw new RuntimeException("PreferenceScreen instance is null");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mHavePrefs = true;
        if (mInitDone) {
            postBindPreferences();
        }
    }

    private void bindPreferences() {
        PreferenceScreen preferencescreen = getPreferenceScreen();
        if (preferencescreen != null) {
            final ListView view = getListView();
            preferencescreen.bind(view);

            view.postDelayed(new Runnable() {

                @Implement
                public void run() {
                    onFinishPreferencesInflate(view);
                }
            }, POST_DELAY);
        }
    }
}
