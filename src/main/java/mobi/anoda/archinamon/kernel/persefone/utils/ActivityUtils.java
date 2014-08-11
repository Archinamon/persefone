package mobi.anoda.archinamon.kernel.persefone.utils;

import android.app.Application;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;

/**
 * Created by Archinamon on 7/8/14.
 */
public final class ActivityUtils {

    public static final String TAG = ActivityUtils.class.getCanonicalName();

    @ProxyMethod
    public static Application getApplicationContext() {
        Application context = null;

        try {
            final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            final Method method = activityThreadClass.getMethod("currentApplication");
            context = (Application) method.invoke(null, (Object[]) null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LogHelper.println_error(TAG, e);
        }

        return context;
    }
}
