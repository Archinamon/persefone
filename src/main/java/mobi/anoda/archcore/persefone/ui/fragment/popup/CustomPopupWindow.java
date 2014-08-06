// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package mobi.anoda.archcore.persefone.ui.fragment.popup;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class CustomPopupWindow {

    protected final View          mActionView;
    protected final PopupWindow   mPopupFrame;
    protected final WindowManager mWindowManager;
    private         View          mContentView;
    private         Drawable      mBackground;

    public CustomPopupWindow(View view) {
        mBackground = null;
        mActionView = view;
        mPopupFrame = new PopupWindow(view.getContext());
        mPopupFrame.a(new OnTouchListener() {

            final CustomPopupWindow mWindow = CustomPopupWindow.this;

            public boolean onTouch(View view1, MotionEvent motionevent) {
                if (motionevent.getAction() == 4) {
                    mWindow.mPopupFrame.c();
                    return true;
                } else {
                    return false;
                }
            }
        });

        Context context = view.getContext();
        if (context != null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        } else {
            mWindowManager = null;
        }

        build();
    }

    protected void build() {
    }

    public void build(View view) {
        mContentView = view;
        mPopupFrame.a(view);
    }

    protected void show() {
        if (mContentView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        if (mBackground == null) {
            mPopupFrame.a(new BitmapDrawable(mPopupFrame.a().getResources()));
        } else {
            mPopupFrame.a(mBackground);
        }
        mPopupFrame.c(-2);
        mPopupFrame.b(-2);
        mPopupFrame.b(true);
        mPopupFrame.a(true);
        mPopupFrame.c(true);
        mPopupFrame.a(mContentView);
    }

    public void display() {
        mPopupFrame.c();
    }
}
