package mobi.anoda.archinamon.kernel.persefone.receiver;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.widget.Toast;
import org.intellij.lang.annotations.MagicConstant;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Permission;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.WordUtils;

/**
 * author: Archinamon project: Multi Locker
 */
public abstract class AbstractReceiver extends BroadcastReceiver {

    public static final    String CUSTOM_DATA = ".custom:key_data";
    protected static final Uri    DATA        = Uri.parse("receiver://mobi.anoda/corelocker/proxy.data/");
    protected final        String TAG         = Common.obtainClassTag(this);
    protected AnodaApplicationDelegate mAppDelegate;

    protected abstract void onReceive(@NonNull final String action, @Nullable Intent data);

    @Implement
    public void onReceive(Context context, Intent intent) {
        if (!(context instanceof Application)) {
            mAppDelegate = (AnodaApplicationDelegate) context.getApplicationContext();
        } else {
            mAppDelegate = (AnodaApplicationDelegate) context;
        }

        final String action = intent.getAction();
        assert action != null;

        onReceive(action, intent);
    }

    protected Context context() {
        return mAppDelegate;
    }

    /* Proxy methods */

    protected Object getSystemService(@MagicConstant(stringValues = {Context.ALARM_SERVICE,
                                                                     Context.ACCESSIBILITY_SERVICE,
                                                                     Context.ACCOUNT_SERVICE,
                                                                     Context.ACTIVITY_SERVICE,
                                                                     Context.APP_OPS_SERVICE,
                                                                     Context.AUDIO_SERVICE,
                                                                     Context.BLUETOOTH_SERVICE,
                                                                     Context.CAPTIONING_SERVICE,
                                                                     Context.CLIPBOARD_SERVICE,
                                                                     Context.CONNECTIVITY_SERVICE,
                                                                     Context.CONSUMER_IR_SERVICE,
                                                                     Context.DEVICE_POLICY_SERVICE,
                                                                     Context.DISPLAY_SERVICE,
                                                                     Context.DOWNLOAD_SERVICE,
                                                                     Context.DROPBOX_SERVICE,
                                                                     Context.INPUT_METHOD_SERVICE,
                                                                     Context.INPUT_SERVICE,
                                                                     Context.KEYGUARD_SERVICE,
                                                                     Context.LAYOUT_INFLATER_SERVICE,
                                                                     Context.LOCATION_SERVICE,
                                                                     Context.MEDIA_ROUTER_SERVICE,
                                                                     Context.NFC_SERVICE,
                                                                     Context.NOTIFICATION_SERVICE,
                                                                     Context.NSD_SERVICE,
                                                                     Context.POWER_SERVICE,
                                                                     Context.PRINT_SERVICE,
                                                                     Context.SEARCH_SERVICE,
                                                                     Context.SENSOR_SERVICE,
                                                                     Context.STORAGE_SERVICE,
                                                                     Context.TELEPHONY_SERVICE,
                                                                     Context.TEXT_SERVICES_MANAGER_SERVICE,
                                                                     Context.UI_MODE_SERVICE,
                                                                     Context.USB_SERVICE,
                                                                     Context.USER_SERVICE,
                                                                     Context.VIBRATOR_SERVICE,
                                                                     Context.WALLPAPER_SERVICE,
                                                                     Context.WIFI_SERVICE,
                                                                     Context.WIFI_P2P_SERVICE,
                                                                     Context.WINDOW_SERVICE}) String serviceName) {
        return context().getSystemService(serviceName);
    }

    protected String getPackageName() {
        return context().getPackageName();
    }

    protected String getString(int id) {
        return context().getString(id);
    }

    protected String getString(int id, Object... formatArgs) {
        return context().getString(id, formatArgs);
    }

    protected void startActivity(Intent i) {
        context().startActivity(i);
    }

    /* Simple broadcast sender*/
    public void sendBroadcast(Broadcastable command) {
        Intent i = new Intent(command.getAction());
        mAppDelegate.sendBroadcast(i);
    }

    protected void sendBroadcast(Broadcastable command, Permission permissions) {
        Intent i = new Intent(command.getAction());
        mAppDelegate.sendBroadcast(i, permissions.getPermission());
    }

    protected void sendBroadcast(Broadcastable command, Bundle data, Permission permissions) {
        Intent i = new Intent(command.getAction());
        i.putExtras(data);

        mAppDelegate.sendBroadcast(i, permissions.getPermission());
    }

    protected void sendBroadcast(String command, Bundle data, Permission permissions) {
        Intent i = new Intent(command);
        i.putExtras(data);

        mAppDelegate.sendBroadcast(i, permissions.getPermission());
    }

    /* Broadcast sender with customizable params */
    protected void sendBroadcast(Broadcastable command, Bundle data) {
        Intent i = new Intent(command.getAction());
        i.putExtras(data);

        mAppDelegate.sendBroadcast(i);
    }

    /* LocalBroadcast sender with customizable params */
    protected void sendLocalBroadcast(Broadcastable command, Bundle data) {
        Intent i = new Intent(command.getAction());
        i.putExtras(data);

        LocalBroadcastManager.getInstance(mAppDelegate).sendBroadcast(i);
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }

    public void startServiceWithAction(Broadcastable command, Class<? extends Service> service) {
        Intent intent = new Intent(mAppDelegate, service);
        intent.setData(DATA);
        intent.setAction(command.getAction());
        mAppDelegate.startService(intent);
    }

    public void startServiceWithAction(Broadcastable command, Class<? extends Service> service, Bundle data) {
        Intent intent = new Intent(mAppDelegate, service);
        intent.setAction(command.getAction());
        intent.setData(DATA);
        intent.putExtras(data);
        mAppDelegate.startService(intent);
    }

    public void startExtService(Broadcastable command, String service, Bundle data) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(mAppDelegate, service));
        intent.setAction(command.getAction());
        intent.setData(DATA);
        intent.putExtras(data);
        mAppDelegate.startService(intent);
    }

    /* Helper to announce message to user */
    protected void shoutToast(String msg) {
        if (WordUtils.isEmpty(msg)) {
            return;
        }

        Toast info = Toast.makeText(mAppDelegate, msg, Toast.LENGTH_SHORT);

        int y = info.getYOffset();
        int x = info.getXOffset();

        info.setGravity(Gravity.TOP | Gravity.CENTER, x / 2, y);
        info.show();
    }
}
