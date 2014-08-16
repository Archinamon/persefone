package mobi.anoda.archinamon.kernel.persefone.service.wakeful;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.receiver.AbstractReceiver;
import mobi.anoda.archinamon.kernel.persefone.service.wakeful.WakefulIntentService.AlarmListener;
import mobi.anoda.archinamon.kernel.persefone.utils.WordUtils;

public class AlarmEventDispatcher extends AbstractReceiver {

    public static final  String TAG               = AlarmEventDispatcher.class.getSimpleName();
    public static final  String EVENT_META_ACTION = TAG + ".signal:dispatch_event";
    private static final String WAKEFUL_META_DATA = "wakefulAlarmProcessor";

    @Implement
    public void onReceive(@Nonnull final String action, @Nullable Intent data) {
        AlarmListener listener = getListener(context());

        if (listener != null) {
            if (!WordUtils.isEmpty(action)) {
                SharedPreferences prefs = context().getSharedPreferences(WakefulIntentService.NAME, 0);

                prefs.edit()
                     .putLong(WakefulIntentService.LAST_ALARM, System.currentTimeMillis())
                     .apply();

                listener.sendWakefulWork(context());
            } else {
                WakefulIntentService.scheduleAlarms(listener, (Application) context(), true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private WakefulIntentService.AlarmListener getListener(Context ctxt) {
        PackageManager pm = ctxt.getPackageManager();
        ComponentName cn = new ComponentName(ctxt, getClass());

        try {
            ActivityInfo ai = pm.getReceiverInfo(cn, PackageManager.GET_META_DATA);
            XmlResourceParser xpp = ai.loadXmlMetaData(pm, WAKEFUL_META_DATA);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("WakefulIntentService")) {
                        String clsName = xpp.getAttributeValue(null, "listener");
                        Class<AlarmListener> cls = (Class<AlarmListener>) Class.forName(clsName);

                        return (cls.newInstance());
                    }
                }

                xpp.next();
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Cannot find own info???", e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Malformed metadata resource XML", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource XML", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Listener class not found", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Listener is not public or lacks public constructor", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create instance of listener", e);
        }

        return (null);
    }
}