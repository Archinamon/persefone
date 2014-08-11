package mobi.anoda.archinamon.kernel.persefone.ui.widget.swipeable;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.widget.swipeable.SwipeHelper.Callback;

/**
 * Copy of packages/apps/UnifiedEmail - com.android.mail.ui.Swipeable with changes.
 */
public class SwipeableListView extends ListView implements Callback {

    private SwipeHelper mSwipeHelper;
    private boolean mEnableSwipe = false;
    private OnItemSwipeListener mOnItemSwipeListener;

    public SwipeableListView(Context context) {
        this(context, null);
    }

    public SwipeableListView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(context, SwipeHelper.X, this, densityScale, pagingTouchSlop);
        setItemsCanFocus(true);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        LogUtils.d(Utils.VIEW_DEBUGGING_TAG,
                "START CLF-ListView.onFocusChanged layoutRequested=%s root.layoutRequested=%s",
                isLayoutRequested(), getRootView().isLayoutRequested());
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        LogUtils.d(Utils.VIEW_DEBUGGING_TAG, new Error(),
                "FINISH CLF-ListView.onFocusChanged layoutRequested=%s root.layoutRequested=%s",
                isLayoutRequested(), getRootView().isLayoutRequested());
    }

    @Override
    public void requestLayout() {
        Utils.checkRequestLayout(this);
        super.requestLayout();
    }

    /**
     * Enable swipe gestures.
     */
    public void enableSwipe(boolean enable) {
        mEnableSwipe = enable;
    }

    public boolean isSwipeEnabled() {
        return mEnableSwipe;
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        mOnItemSwipeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnableSwipe) {
            return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnableSwipe) {
            return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Implement
    public View getChildAtPosition(MotionEvent ev) {
        // find the view under the pointer, accounting for GONE views
        final int count = getChildCount();
        int touchY = (int) ev.getY();
        int childIdx = 0;
        View slidingChild;
        for (; childIdx < count; childIdx++) {
            slidingChild = getChildAt(childIdx);
            if (slidingChild.getVisibility() == GONE) {
                continue;
            }
            if (touchY >= slidingChild.getTop() && touchY <= slidingChild.getBottom()) {
                return slidingChild;
            }
        }
        return null;
    }

    @Implement
    public View getChildContentView(View view) {
        return view;
    }

    @Implement
    public void onScroll() {
    }

    @Implement
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Implement
    public void onChildDismissed(final View v) {
        if (v != null) {
            if (mOnItemSwipeListener != null) {
                mOnItemSwipeListener.onSwipe(v);
            }
        }
    }

    @Implement
    public void onDragCancelled(View v) {
    }

    private void redraw(View v) {
        int start = getFirstVisiblePosition();
        for (int i=start, j = getLastVisiblePosition(); i <= j; i++) {
            if (v == getItemAtPosition(i)) {
                View view = getChildAt(i-start);
                getAdapter().getView(i, view, this);
            }
        }
    }

    @Implement
    public void onBeginDrag(View v, float diff) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }

    public interface OnItemSwipeListener {
        public void onSwipe(View view);
    }
}
