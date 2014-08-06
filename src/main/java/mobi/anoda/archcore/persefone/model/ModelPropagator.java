package mobi.anoda.archcore.persefone.model;

import android.os.Parcelable;
import android.util.Log;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.annotation.ProjectionTag;
import mobi.anoda.archcore.persefone.utils.LogHelper;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class ModelPropagator implements Parcelable,
                                                 ProjectionUpdater {

    protected static final String USING_REFLECT     = "unused";
    protected static final String REFLECTIVE_ASSIGN = "unchecked";

    protected ModelPropagator(ImmutableMap map) {
        if (map == null) {
            return;
        }

        //trick to avoid "Ambiguous method call" error
        final Class klass = ((Object) this).getClass();

        Method[] methods;
        if (klass.getSuperclass() != ModelPropagator.class) {
            Method[] superMethods = klass.getSuperclass().getDeclaredMethods();
            Method[] instMethods = klass.getDeclaredMethods();

            methods = new Method[instMethods.length + superMethods.length];
            System.arraycopy(superMethods, 0, methods, 0, superMethods.length);
            System.arraycopy(instMethods, 0, methods, superMethods.length, instMethods.length);
        } else {
            methods = klass.getDeclaredMethods();
        }

        for (Method m : methods) {
            if (m.isAnnotationPresent(ProjectionTag.class)) {
                ProjectionTag anno = m.getAnnotation(ProjectionTag.class);
                String call = anno.value();

                if (map.containsKey(call)) {
                    boolean isAccessable = true;
                    try {
                        if (!(isAccessable = m.isAccessible())) {
                            m.setAccessible(true);
                        }

                        m.invoke(this, map.get(call));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(klass.getSimpleName(),
                              e.getMessage() != null
                              ? call + " : " + e.getMessage()
                              : call + ": unexpected exception");
                    } finally {
                        if (!isAccessable) {
                            m.setAccessible(false);
                        }
                    }
                }
            }
        }
    }

    public abstract boolean isNull();

    @Implement
    public final int describeContents() {
        return 0;
    }

    protected <T> void addToProjector(String column, T data) {
        if (data instanceof Byte) {
            MODEL_CONTENT.put(column, (Byte) data);
        } else if (data instanceof String) {
            MODEL_CONTENT.put(column, (String) data);
        } else if (data instanceof Short) {
            MODEL_CONTENT.put(column, (Short) data);
        } else if (data instanceof Integer) {
            MODEL_CONTENT.put(column, (Integer) data);
        } else if (data instanceof Boolean) {
            MODEL_CONTENT.put(column, (Boolean) data);
        } else if (data instanceof Long) {
            MODEL_CONTENT.put(column, (Long) data);
        } else if (data instanceof Float) {
            MODEL_CONTENT.put(column, (Float) data);
        } else if (data instanceof Double) {
            MODEL_CONTENT.put(column, (Double) data);
        } else if (data instanceof byte[]) {
            MODEL_CONTENT.put(column, (byte[]) data);
        } else {
            MODEL_CONTENT.put(column, data != null ? data.toString() : "");
        }
    }

    protected void printError(Exception e) {
        final Class klass = ((Object) this).getClass();

        LogHelper.println_verbose(klass.getSimpleName(), e);
    }
}
