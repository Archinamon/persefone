package mobi.anoda.archinamon.kernel.persefone.service.wakeful;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractIntentService;

public abstract class WakefulIntentService extends AbstractIntentService {

    public interface AlarmListener {

        void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Intent i, Application application);

        void sendWakefulWork(Context context, Intent intent);

        long getMaxAge();
    }

    static final            String                NAME       = WakefulIntentService.class.getName();
    static final            String                LAST_ALARM = "lastAlarm";
    private static volatile PowerManager.WakeLock sWakeLock  = null;

    private synchronized static PowerManager.WakeLock getLock(Context context) {
        if (sWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            sWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
            sWakeLock.setReferenceCounted(true);
        }

        return sWakeLock;
    }

    public static void sendWakefulWork(Context context, Intent i) {
        getLock(context.getApplicationContext()).acquire();
        context.startService(i);
    }

    public static void sendWakefulWork(Context context, Class<?> clsService) {
        sendWakefulWork(context, new Intent(context, clsService));
    }

    public static void scheduleAlarms(AlarmListener listener, Application application) {
        scheduleAlarms(listener, application, true);
    }

    public static void scheduleAlarms(AlarmListener listener, Application application, boolean force) {
        SharedPreferences prefs = application.getSharedPreferences(NAME, 0);
        long lastAlarm = prefs.getLong(LAST_ALARM, 0);

        if (lastAlarm == 0 || force || (System.currentTimeMillis() > lastAlarm && System.currentTimeMillis() - lastAlarm > listener.getMaxAge())) {
            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(application, AlarmEventDispatcher.class);
            i.setAction(AlarmEventDispatcher.EVENT_META_ACTION);
            PendingIntent pi = PendingIntent.getBroadcast(application, 0, i, 0);

            listener.scheduleAlarms(mgr, pi, i, application);
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmEventDispatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        mgr.cancel(pi);

        context.getSharedPreferences(NAME, 0)
            .edit()
            .remove(LAST_ALARM)
            .commit();
    }

    public WakefulIntentService(String name) {
        super(name);
        setIntentRedelivery(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager.WakeLock lock = getLock(this.getApplicationContext());

        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
            lock.acquire();
        }

        super.onStartCommand(intent, flags, startId);

        return START_REDELIVER_INTENT;
    }

    @Override
    final protected void onHandleIntent(Intent intent) {
        try {
            doWakefulWork(intent);
        } finally {
            PowerManager.WakeLock lock = getLock(this.getApplicationContext());

            if (lock.isHeld()) {
                lock.release();
            }
        }
    }

    protected abstract void doWakefulWork(Intent intent);
}