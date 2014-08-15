package mobi.anoda.archinamon.kernel.persefone.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class Common {

    public static final String TAG          = Common.class.getSimpleName();
    public static final String EMPTY_STRING = "";

    public static String obtainClassTag(Object i) {
        return i.getClass().getSimpleName();
    }

    public static float getDensityMultiplier(Context c) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static String getApplicationAgent(Context context, String ua) {
        StringBuilder agentBuilder = new StringBuilder(ua + "/1.8 (Android-");
        agentBuilder.append(Build.MANUFACTURER)
                    .append(Build.DEVICE)
                    .append(Build.MODEL)
                    .append("; API-")
                    .append(Build.VERSION.SDK_INT)
                    .append("; Scale/")
                    .append(Common.getDensityMultiplier(context))
                    .append(") ")
                    .append(MetricsHelper.isTablet(context) ? "AndroidTablet" : "AndroidPhone");

        return agentBuilder.toString();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String saveImage(Context context, Bitmap source, String name) {
        File cache = new File(context.getCacheDir(), name);
        try {
            FileOutputStream fos = new FileOutputStream(cache);
            source.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            LogHelper.println_error(TAG, e);
        }

        return cache.getPath();
    }

    @SuppressWarnings("deprecation")
    public static String getPathFromURI(Activity context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.managedQuery(contentUri, proj, null, null, null);
        if (cursor == null) {
            // might be already absolute path;
            return contentUri.toString();
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @SuppressWarnings("deprecation")
    public static int getRotation(Activity context, Uri uri) throws IOException {
        ExifInterface exif = new ExifInterface(uri.getPath());
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = context.managedQuery(uri, orientationColumn, null, null, null);

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }

        return orientation;
    }

    public static int[] getDisplayMetrics(Context context) {
        final int[] result = new int[2];
        final Point point = new Point();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        display.getRealSize(point);
        result[0] = point.x;
        result[1] = point.y;
        return result;
    }

    public static String getApplicationName(Context ctx) {
        ApplicationInfo ai;
        try {
            Context appContext = ctx.getApplicationContext();
            assert appContext != null;
            final PackageManager pm = appContext.getPackageManager();
            assert pm != null;
            ai = pm.getApplicationInfo(ctx.getPackageName(), 0);
            return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Check if package installed
     *
     * @param context Context of current app
     * @param uri Package of application to check
     * @return true if passed package installed
     */
    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean appInstalled;
        try {
            assert pm != null;
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }

    /**
     * Check if action available installed
     *
     * @param context Context of current app
     * @param action Package of application to check
     * @return true if passed package installed
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        assert packageManager != null;
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void openApplicationByName(AbstractActivity context, final String appName) {
        PackageManager manager = context.getPackageManager();
        assert manager != null;

        Intent intent = manager.getLaunchIntentForPackage(appName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + appName));

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                context.shoutToast(context.getString(R.string.google_play_app_missing));
            }
        }
    }

    public static void requestUninstallApp(Context context, String uri) {
        Uri packageUri = Uri.parse("package:" + uri);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }

    public static boolean isActivityRunning(Context context, Class activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        if (ListUtils.isEmpty(tasks)) return false;

        if (tasks != null) {
            for (RunningTaskInfo task : tasks) {
                ComponentName info = task.baseActivity;
                if (info != null) {
                    String activityName = activityClass.getCanonicalName();
                    String packageName = info.getPackageName();
                    if (activityName.equals(task.baseActivity.getClassName()) && (packageName != null && packageName.equals(context.getPackageName())))
                        return true;
                }
            }
        }

        return false;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager manager = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningServiceInfo> list = manager.getRunningServices(Integer.MAX_VALUE);
        assert list != null;

        Iterator iterator = list.iterator();
        do {
            if (!iterator.hasNext()) {
                return false;
            }
        } while (!serviceName.equals(((ActivityManager.RunningServiceInfo) iterator.next()).service.getClassName()));

        return true;
    }

    public static String getMarketLink(Context context) {
        return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
    }

    public static String getMarketSchemaLink(Context context) {
        return "market://details?id=" + context.getPackageName();
    }
}
