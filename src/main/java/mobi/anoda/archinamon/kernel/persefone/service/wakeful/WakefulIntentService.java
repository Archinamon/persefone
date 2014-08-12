package mobi.anoda.archinamon.kernel.persefone.service.wakeful;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractIntentService;

public abstract class WakefulIntentService extends AbstractIntentService {

    public interface AlarmListener {

        void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctxt);

        void sendWakefulWork(Context ctxt);

        long getMaxAge();
    }

    static final            String                NAME       = WakefulIntentService.class.getName();
    static final            String                LAST_ALARM = "lastAlarm";
    private static volatile PowerManager.WakeLock lockStatic = null;

    private synchronized static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
            lockStatic.setReferenceCounted(true);
        }

        return lockStatic;
    }

    public static void sendWakefulWork(Context ctxt, Intent i) {
        getLock(ctxt.getApplicationContext()).acquire();
        ctxt.startService(i);
    }

    public static void sendWakefulWork(Context ctxt, Class<?> clsService) {
        sendWakefulWork(ctxt, new Intent(ctxt, clsService));
    }

    public static void scheduleAlarms(AlarmListener listener, Context ctxt) {
        scheduleAlarms(listener, ctxt, true);
    }

    public static void scheduleAlarms(AlarmListener listener, Context ctxt, boolean force) {
        SharedPreferences prefs = ctxt.getSharedPreferences(NAME, 0);
        long lastAlarm = prefs.getLong(LAST_ALARM, 0);

        if (lastAlarm == 0 || force || (System.currentTimeMillis() > lastAlarm && System.currentTimeMillis() - lastAlarm > listener.getMaxAge())) {
            AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(ctxt, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

            listener.scheduleAlarms(mgr, pi, ctxt);
        }
    }

    public static void cancelAlarms(Context ctxt) {
        AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctxt, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

        mgr.cancel(pi);

        ctxt.getSharedPreferences(NAME, 0)
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