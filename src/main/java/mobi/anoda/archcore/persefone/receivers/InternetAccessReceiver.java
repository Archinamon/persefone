package mobi.anoda.archcore.persefone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import mobi.anoda.archcore.persefone.network.State;
import mobi.anoda.archcore.persefone.services.NetworkNotification;
import mobi.anoda.archcore.persefone.utils.LogHelper;

/** author Archinamon */
public class InternetAccessReceiver extends BroadcastReceiver {

    public static final    String  TAG             = InternetAccessReceiver.class.getSimpleName();
    public static final    String  ACCESS_GRANTED  = NetworkNotification.INTERNET_ACCESS_GRANTED.getAction();
    public static volatile Boolean gInternetAccess = true;
    private static final   String  MOBILE          = "MOBILE";
    private static final   String  WIFI            = "WIFI";
    private final          Object  MUTEX           = new Object();

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (MUTEX) {
            boolean isConnected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            boolean additionalCheck = checkConnection(context);

            LogHelper.println_verbose(TAG, String.valueOf(isConnected && additionalCheck));
            gInternetAccess = isConnected && additionalCheck;

            State.svAccessState = gInternetAccess ? State.ACCESS_GRANTED
                                                  : State.ACCESS_DENIED;

            if (gInternetAccess) {
                context.sendBroadcast(new Intent(ACCESS_GRANTED));
            }
        }
    }

    public static boolean checkConnection(Context c) {
        boolean result = false;

        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        assert netInfo != null;

        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(WIFI)) {
                if (ni.isConnected() && ni.isAvailable()) {
                    result = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase(MOBILE)) {
                if (ni.isConnected() && ni.isAvailable()) {
                    result = true;
                }
            }
        }

        return result;
    }
}
