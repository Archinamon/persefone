package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
public final class SoftKeyboard {

    public interface OnFocusLoseListener {

        void onClearFocus(View view);
    }

    private final StableContext       mStableContext;
    private final InputMethodManager  mInputManagerService;
    private       OnFocusLoseListener mListener;

    public SoftKeyboard(StableContext stableContext) {
        this.mStableContext = stableContext;
        this.mInputManagerService = (InputMethodManager) stableContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /* Helper to hide soft input KeyboardView */
    public void hideSoftInput(final int view) {
        final View viewInstance = mStableContext.findViewById(view);

        if (viewInstance != null)
            mInputManagerService.hideSoftInputFromWindow(viewInstance.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void hideSoftInput() {
        final Window win = mStableContext.getWindow();
        if (win != null) {
            View decor = win.getDecorView();
            mInputManagerService.hideSoftInputFromWindow(decor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void hideSoftInput(final View view) {
        mInputManagerService.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /* Helper to show soft input KeyboardView */
    public void showSoftInput(View v) {
        mInputManagerService.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    public void setOnFocusLoseListener(OnFocusLoseListener listener) {
        mListener = listener;
    }

    public void initKeyboardShowListener() {
        final View view = mStableContext.findViewById(R.id.touch_interceptor);
        if (view != null) {
            ViewTreeObserver observer = view.getViewTreeObserver();
            if (observer != null) {
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Implement
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        view.getWindowVisibleDisplayFrame(r);
                        int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
                        if (heightDiff <= 100) {
                            clearFocus();
                        }
                    }
                });
            }
        }
    }

    public void initTouchInterceptor() {
        FrameLayout touchInterceptor = (FrameLayout) mStableContext.findViewById(R.id.touch_interceptor);
        if (touchInterceptor != null) {
            touchInterceptor.setOnTouchListener(new View.OnTouchListener() {

                @Implement
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (mStableContext.isUiContextRegistered()) {
                            final View focusedView = mStableContext.getCurrentFocus();
                            if (focusedView instanceof EditText) {
                                if (!isPointInsideView(event.getRawX(), event.getRawY(), focusedView)) {
                                    if (!isTouchOnEditText(event.getRawX(), event.getRawY(), mStableContext.getView())) {
                                        clearFocus();
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    public void clearFocus() {
        if (mStableContext.isUiContextRegistered()) {
            final View focusedView = mStableContext.getCurrentFocus();
            if (focusedView != null) {
                focusedView.clearFocus();
                focusedView.setFocusable(false);
                focusedView.setFocusableInTouchMode(true);

                if (mListener != null) {
                    mListener.onClearFocus(focusedView);
                }

                hideSoftInput(focusedView);
            }
        }
    }

    private boolean isTouchOnEditText(float x, float y, View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof ViewGroup) {
                    boolean isTouchOnEditText = isTouchOnEditText(x, y, child);
                    if (isTouchOnEditText) {
                        return true;
                    }
                } else if (child instanceof EditText && isPointInsideView(x, y, child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if given points are inside view
     *
     * @param x    - x coordinate of point
     * @param y    - y coordinate of point
     * @param view - view object to compare
     * @return true if the points are within view bounds, false otherwise
     */
    private boolean isPointInsideView(float x, float y, View view) {
        Rect outRect = new Rect();
        view.getGlobalVisibleRect(outRect);
        return outRect.contains((int) x, (int) y);
    }
}
