package mobi.anoda.archinamon.kernel.persefone.ui.popup;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

public class CustomPopupWindow {

    protected final View          mActionView;
    protected final PopupWindow   mPopupFrame;
    protected final WindowManager mWindowManager;
    private         View          mContentView;
    private         Drawable      mBackground;

    public CustomPopupWindow(View anchor) {
        mBackground = null;
        mActionView = anchor;
        mPopupFrame = new PopupWindow(anchor.getContext());
        mPopupFrame.setTouchInterceptor(new OnTouchListener() {

            @Implement
            public boolean onTouch(View view1, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupFrame.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });

        Context context = anchor.getContext();
        if (context != null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        } else {
            mWindowManager = null;
        }

        preBuild();
    }

    protected void preBuild() {
    }

    protected void build(View view) {
        mContentView = view;
        mPopupFrame.setContentView(view);
    }

    protected void show() {
        if (mContentView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        if (mBackground == null) {
            mPopupFrame.setBackgroundDrawable(new BitmapDrawable(mPopupFrame.getContentView().getResources()));
        } else {
            mPopupFrame.setBackgroundDrawable(mBackground);
        }
        mPopupFrame.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupFrame.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupFrame.setTouchable(true);
        mPopupFrame.setFocusable(true);
        mPopupFrame.setOutsideTouchable(true);
        mPopupFrame.setContentView(mContentView);
    }

    public void dismiss() {
        mPopupFrame.dismiss();
    }
}
