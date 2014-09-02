package mobi.anoda.archinamon.kernel.persefone.ui.widget.adapterview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 * <p/>
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 * <p/>
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 *
 * author Chris Banes
 */
public class ViewPager extends android.support.v4.view.ViewPager {

    public static final String TAG = ViewPager.class.getSimpleName();
    private boolean mIsEnabled;

    public ViewPager(Context context) {
        this(context, null);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsEnabled = true;
    }

    public boolean isPagingEnabled() {
        return mIsEnabled;
    }

    public void setPagingEnabled(boolean flag) {
        mIsEnabled = flag;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsEnabled) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                LogHelper.println_error(TAG, e);
                return false;
            }
        } else {
            return false;
        }
    }
}
