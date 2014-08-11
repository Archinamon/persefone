package mobi.anoda.archinamon.kernel.persefone.ui.widget.swipeable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import org.jetbrains.annotations.NotNull;

/**
 * Layout which always handles the touch event.
 * <p>
 * This is needed by the SwipeableListView onInterceptTouchEvent()
 *
 */
public class SwipeLayout extends LinearLayout {

    public SwipeLayout(Context context) {
        super(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(@NotNull MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}
