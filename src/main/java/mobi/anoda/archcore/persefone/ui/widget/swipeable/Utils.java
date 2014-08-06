package mobi.anoda.archcore.persefone.ui.widget.swipeable;

import android.view.View;

public class Utils {

    public static final String VIEW_DEBUGGING_TAG = "Persefone";

    public static void checkRequestLayout(View v) {
        boolean inLayout = false;
        final View root = v.getRootView();

        if (root == null || v.isLayoutRequested()) {
            return;
        }

        final Error e = new Error();
        for (StackTraceElement ste : e.getStackTrace()) {
            if ("android.view.ViewGroup".equals(ste.getClassName())
                && "layout".equals(ste.getMethodName())) {
                inLayout = true;
                break;
            }
        }
        if (inLayout && !v.isLayoutRequested()) {
            LogUtils.i(VIEW_DEBUGGING_TAG,
                       e, "WARNING: in requestLayout during layout pass, view=%s", v);
        }
    }
}
