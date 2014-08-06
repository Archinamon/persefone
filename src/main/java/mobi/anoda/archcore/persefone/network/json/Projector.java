package mobi.anoda.archcore.persefone.network.json;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import mobi.anoda.archcore.persefone.annotation.DataHolderLink;

/**
 * @author: Archinamon
 */
public class Projector {

    public static final String TAG = Projector.class.getSimpleName();
    private static Projector INSTANCE;
    private final Object MUTEX = new Object();
    private HashMap mMap;

    public static <T> Projector getInstance(T map) {
        if (INSTANCE == null) {
            INSTANCE = new Projector(map);
        }

        return INSTANCE;
    }

    public static void recycle() {
        INSTANCE.mMap = null;
        INSTANCE = null;
    }

    private <T> Projector(T base) {
        synchronized (MUTEX) {
            mMap = (HashMap) base;
        }
    }

    public Projector parse(JSONObject object, Class projection) {
        try {
            parseFields(object, projection);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,
                  e.getMessage() != null
                  ? e.getMessage()
                  : "unexpected exception");
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private void parseFields(JSONObject obj, Class klass) throws IllegalAccessException,
                                                                 InstantiationException,
                                                                 JSONException {
        Object instance = klass.newInstance();
        Field[] fields = getFields(klass);
        Annotation projection = klass.getAnnotation(DataHolderLink.class);

        if (projection != null) {
            DataHolderLink holder = (DataHolderLink) projection;
            obj = obj.getJSONObject(holder.value());
        }

        for (Field f : fields) {
            Object preCast = f.get(instance);
            String casted;

            assert preCast instanceof String;
            casted = (String) preCast;

            synchronized (MUTEX) {
                Object item = obj.opt(casted);

                if (item == null) continue;

                if (item instanceof JSONObject) {
                    mMap.put(casted, new ProjectionObject(obj.get(casted).toString()));
                } else if (item instanceof JSONArray) {
                    mMap.put(casted, new ProjectionArray(obj.get(casted).toString()));
                } else {
                    mMap.put(casted, obj.get(casted));
                }
            }
        }
    }

    private Field[] getFields(Class baseType) {
        return baseType.getDeclaredFields();
    }

    public HashMap get() {
        synchronized (MUTEX) {
            return mMap;
        }
    }
}
