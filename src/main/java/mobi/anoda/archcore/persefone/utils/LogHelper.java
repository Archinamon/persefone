package mobi.anoda.archcore.persefone.utils;

import android.util.Log;
import mobi.anoda.archcore.persefone.AnodaApplicationDelegate;

/**
 * author: Archinamon
 * project: FavorMe
 */
@SuppressWarnings("FinalStaticMethod")
public class LogHelper {

    private static final boolean MODE = AnodaApplicationDelegate.DEBUG;

    public static final void println_verbose(String tag, String msg) {
        if (MODE) {
            Log.w(tag, !WordUtils.isEmpty(msg) ? msg : "Silent exception occurred");
        }
    }

    public static final void println_verbose(String tag, Throwable what) {
        if (MODE) {
            what.printStackTrace();
            Log.w(tag, what.getMessage() != null ? what.getMessage() : "Silent exception occurred");
        }
    }

    public static final void println_error(String tag, Throwable what) {
        if (MODE) {
            what.printStackTrace();
            Log.w(tag, what.getMessage() != null ? what.getMessage() : "Silent exception occurred");
        }
    }

    public static final void println_info(String tag, Throwable what) {
        if (MODE) {
            what.printStackTrace();
            Log.w(tag, what.getMessage() != null ? what.getMessage() : "Silent exception occurred");
        }
    }
}
