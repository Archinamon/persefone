package mobi.anoda.archinamon.kernel.persefone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;
import java.io.IOException;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class MetricsHelper {

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
        display.getSize(point);
        result[0] = point.x;
        result[1] = point.y;
        return result;
    }

    public static void calcSmallPopupMetrics(Activity a, int[] wh) {
        final int MAX_WIDTH = 300;
        final int MAX_HEIGHT = 200;

        Point metrics = new Point();
        final float multiplier = internal_prepareMetrics(a, metrics);

        internal_calculatePopupMetrics(wh, multiplier, metrics, new int[] {MAX_HEIGHT, MAX_WIDTH});
    }

    public static void calcLargePopupMetrics(Activity a, int[] wh) {
        final int MAX_WIDTH = 450;
        final int MAX_HEIGHT = 650;

        Point metrics = new Point();
        final float multiplier = internal_prepareMetrics(a, metrics);

        internal_calculatePopupMetrics(wh, multiplier, metrics, new int[] {MAX_HEIGHT, MAX_WIDTH});
    }

    public static boolean isTablet(Context context) {
        Resources res = context.getResources();
        return (res.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static float internal_prepareMetrics(Activity a, final Point viewPort) {
        Display display = a.getWindowManager().getDefaultDisplay();
        display.getSize(viewPort);
        return Common.getDensityMultiplier(a);
    }

    private static void internal_calculatePopupMetrics(final int[] resultWH, final float density, final Point metrics, final int[] requerements) {
        int viewPortHeight = (int) ((float) metrics.y / density);
        if (viewPortHeight > requerements[0]) {
            resultWH[0] = (int) (requerements[0] * density);
        } else {
            resultWH[0] = metrics.y;
        }

        int viewPortWidth = (int) ((float) metrics.x / density);
        if (viewPortWidth > requerements[1]) {
            resultWH[1] = (int) (requerements[1] * density);
        } else {
            resultWH[1] = metrics.x;
        }
    }
}
