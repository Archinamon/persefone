package mobi.anoda.archinamon.kernel.persefone.utils;

import android.app.Application;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;

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
        } finally {
            if (context == null) {
                context = (Application) AnodaApplicationDelegate.getProxyContext();
            }
        }

        return context;
    }

    public static String getTextFromView(@IdRes int viewId) {
        StableContext context = StableContext.Impl.obtain();
        View v = context.findViewById(viewId);
        if (v != null) {
            if (v instanceof TextView) {
                TextView view = (TextView) v;
                return view.getText()
                           .toString();
            } else {
                return v.toString();
            }
        } else return WordUtils.EMPTY;
    }
}
