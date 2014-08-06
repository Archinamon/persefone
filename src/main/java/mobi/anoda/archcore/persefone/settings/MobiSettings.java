package mobi.anoda.archcore.persefone.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mobi.anoda.archcore.persefone.utils.LogHelper;
import mobi.anoda.archcore.persefone.utils.WordUtils;

@SuppressWarnings("FinalStaticMethod")
public abstract class MobiSettings {

    public interface IInterference {

        void fixation();

        void change();
    }

    private static final         Gson                      GSON                = new Gson();
    private static final         String                    PREFS_NAME          = "settings::archcore.persefone.application";
    private static final         Map<String, MobiSettings> sfSettingsInstances = new ConcurrentHashMap<>();
    protected transient final    Object                    tfObjectMutex       = new Object();
    protected transient volatile boolean                   vChangeFixation     = false;
    protected transient volatile long                     vFixationTime;
    protected transient volatile long                     vChangeTime;
    protected                    SharedPreferences        mPreferences;
    protected                    SharedPreferences.Editor mEditor;
    private                      Context                  mContext;
    private                      String                   mTag;

    public static final String getPreferencesName() {
        return PREFS_NAME;
    }

    @SuppressWarnings("unchecked")
    public static <Prefs extends MobiSettings> Prefs getInstance(Context context, String name, Class<Prefs> klass) {
        if (sfSettingsInstances.containsKey(name)) {
            return (Prefs) sfSettingsInstances.get(name);
        }

        Prefs instance = null;
        if (klass != null) {
            try {
                instance = klass.newInstance();
                instance.attachContext(context);
                instance.setTag(name);
                instance.postInit();

                sfSettingsInstances.put(name, instance);
            } catch (InstantiationException | IllegalAccessException e) {
                LogHelper.println_error(PREFS_NAME, e);
            }
        }

        return instance;
    }

    protected final void setTag(String tag) {
        if (WordUtils.isEmpty(mTag)) mTag = tag;
        else throw new IllegalStateException("instance already have a tag");
    }

    protected final Context getContext() {
        return mContext;
    }

    protected abstract void postInit();

    public abstract void save();

    public abstract void restore();

    public abstract void erase();

    protected final void dispose() {
        sfSettingsInstances.remove(mTag);
    }

    protected void attachContext(Context context) {
        this.mContext = context;
    }

    protected SharedPreferences structPrivatePreferences() {
        return __getPrefs(Context.MODE_PRIVATE);
    }

    protected SharedPreferences structMultiProcessPreferences() {
        return __getPrefs(Context.MODE_MULTI_PROCESS);
    }

    protected void putObject(String key, Object object) {
        if(object == null){
            throw new IllegalArgumentException("object is null");
        }

        if(key.equals("") || key == null){
            throw new IllegalArgumentException("key is empty or null");
        }

        mEditor.putString(key, GSON.toJson(object));
    }

    protected final void commit() {
        mEditor.apply();
    }

    protected <T> T getObject(String key, Class<T> a) {
        String gson = mPreferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try{
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storaged with key " + key + " is instanceof other class");
            }
        }
    }

    private SharedPreferences __getPrefs(int bitMask) {
        if (mPreferences == null) {
            mPreferences = mContext.getSharedPreferences(PREFS_NAME, bitMask);
        }

        if (mEditor == null) {
            mEditor = mPreferences.edit();
        }

        return mPreferences;
    }
}