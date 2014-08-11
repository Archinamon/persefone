// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package mobi.anoda.archinamon.kernel.persefone.ui.fragment.popup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.lang.ref.WeakReference;
import mobi.anoda.archinamon.kernel.persefone.R;

public class PopupWindow {

    private static final int F[] = {0x7f01010b};
    private boolean           A;
    private int               B;
    private OnDismissListener C;
    private boolean           D;
    private int               E;
    private WeakReference     G;
    private ViewTreeObserver.OnScrollChangedListener H = new ViewTreeObserver.OnScrollChangedListener() {

        final PopupWindow a = PopupWindow.this;

        public void onScrollChanged() {
            View view = (View) PopupWindow.a(a)
                                          .get();
            if (view != null && PopupWindow.b(a) != null) {
                WindowManager.LayoutParams layoutparams = (WindowManager.LayoutParams) PopupWindow.b(a).getLayoutParams();
                PopupWindow.a(a, PopupWindow.a(a, view, layoutparams, PopupWindow.c(a), PopupWindow.d(a)));
                a.a(layoutparams.x, layoutparams.y, -1, -1, true);
            }
        }
    };
    private int                  I;
    private int                  J;
    private Context              a;
    private WindowManager        b;
    private boolean              c;
    private boolean              d;
    private View                 e;
    private View                 f;
    private boolean              g;
    private int                  h;
    private int                  i;
    private boolean              j;
    private boolean              k;
    private boolean              l;
    private boolean              m;
    private View.OnTouchListener n;
    private int                  o;
    private int                  p;
    private int                  q;
    private int                  r;
    private int                  s;
    private int                  t;
    private int                  u;
    private int                  v;
    private int                  w[];
    private int                  x[];
    private Rect                 y;
    private Drawable             z;

    public PopupWindow() {
        this(null, 0, 0);
    }

    public PopupWindow(Context context) {
        this(context, null);
    }

    public PopupWindow(Context context, AttributeSet attributeset) {
        this(context, attributeset, 0x7f010108);
    }

    public PopupWindow(Context context, AttributeSet attributeset, int i1) {
        h = 0;
        j = true;
        k = false;
        l = true;
        w = new int[2];
        x = new int[2];
        y = new Rect();
        B = 1000;
        D = false;
        E = -1;
        a = context;
        b = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        TypedArray typedarray = context.obtainStyledAttributes(attributeset, R.styleable.PopupWindow, i1, 0);
        z = typedarray.getDrawable(0);
        int j1 = typedarray.getResourceId(1, -1);
        if (j1 == 0x7f0d008e) { //Animation_PopupWindow
            j1 = -1;
        }
        E = j1;
        typedarray.recycle();
    }

    public PopupWindow(View view, int i1, int j1) {
        this(view, i1, j1, false);
    }

    public PopupWindow(View view, int i1, int j1, boolean flag) {
        h = 0;
        j = true;
        k = false;
        l = true;
        w = new int[2];
        x = new int[2];
        y = new Rect();
        B = 1000;
        D = false;
        E = -1;
        if (view != null) {
            a = view.getContext();
            b = (WindowManager) a.getSystemService(Context.WINDOW_SERVICE);
        }
        a(view);
        c(i1);
        b(j1);
        a(flag);
    }

    private WindowManager.LayoutParams a(IBinder ibinder) {
        WindowManager.LayoutParams layoutparams = new WindowManager.LayoutParams();
        layoutparams.gravity = 51;
        int i1 = p;
        q = i1;
        layoutparams.width = i1;
        int j1 = s;
        t = j1;
        layoutparams.height = j1;
        if (z != null) {
            layoutparams.format = z.getOpacity();
        } else {
            layoutparams.format = -3;
        }
        layoutparams.flags = d(layoutparams.flags);
        layoutparams.type = B;
        layoutparams.token = ibinder;
        layoutparams.softInputMode = i;
        layoutparams.setTitle((new StringBuilder("PopupWindow:")).append(Integer.toHexString(hashCode()))
                                                                 .toString());
        return layoutparams;
    }

    static WeakReference a(PopupWindow popupwindow) {
        return popupwindow.G;
    }

    private void a(WindowManager.LayoutParams layoutparams) {
        byte byte0 = -2;
        if (e == null || a == null || b == null) {
            throw new IllegalStateException("You must specify a valid content view by calling setContentView() before attempting to show the popup.");
        }
        if (z != null) {
            ViewGroup.LayoutParams layoutparams1 = e.getLayoutParams();
            PopupViewContainer popupviewcontainer;
            FrameLayout.LayoutParams layoutparams2;
            if (layoutparams1 == null || layoutparams1.height != byte0) {
                byte0 = -1;
            }
            popupviewcontainer = new PopupViewContainer(a);
            layoutparams2 = new FrameLayout.LayoutParams(-1, byte0);
            popupviewcontainer.setBackgroundDrawable(z);
            popupviewcontainer.addView(e, layoutparams2);
            f = popupviewcontainer;
        } else {
            f = e;
        }
        u = layoutparams.width;
        v = layoutparams.height;
    }

    static void a(PopupWindow popupwindow, boolean flag) {
        popupwindow.d(flag);
    }

    private boolean a(View view, WindowManager.LayoutParams layoutparams, int i1, int j1) {
        boolean flag;
        label0:
        {
            view.getLocationInWindow(w);
            layoutparams.x = i1 + w[0];
            layoutparams.y = j1 + (w[1] + view.getHeight());
            layoutparams.gravity = 51;
            view.getLocationOnScreen(x);
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            View view1 = view.getRootView();
            if (layoutparams.y + v <= rect.bottom) {
                int k2 = (layoutparams.x + u) - view1.getWidth();
                flag = false;
                if (k2 <= 0) {
                    break label0;
                }
            }
            int k1 = view.getScrollX();
            int l1 = view.getScrollY();
            view.requestRectangleOnScreen(new Rect(k1, l1, i1 + (k1 + u), j1 + (l1 + v + view.getHeight())), true);
            view.getLocationInWindow(w);
            layoutparams.x = i1 + w[0];
            layoutparams.y = j1 + (w[1] + view.getHeight());
            view.getLocationOnScreen(x);
            int i2 = rect.bottom - x[1] - view.getHeight() - j1;
            int j2 = x[1] - j1 - rect.top;
            flag = false;
            if (i2 < j2) {
                flag = true;
            }
            if (flag) {
                layoutparams.gravity = 83;
                layoutparams.y = j1 + (view1.getHeight() - w[1]);
            } else {
                layoutparams.y = j1 + (w[1] + view.getHeight());
            }
        }
        layoutparams.gravity = 0x10000000 | layoutparams.gravity;
        return flag;
    }

    static boolean a(PopupWindow popupwindow, View view, WindowManager.LayoutParams layoutparams, int i1, int j1) {
        return popupwindow.a(view, layoutparams, i1, j1);
    }

    static View b(PopupWindow popupwindow) {
        return popupwindow.f;
    }

    private void b(WindowManager.LayoutParams layoutparams) {
        layoutparams.packageName = a.getPackageName();
        b.addView(f, layoutparams);
    }

    static int c(PopupWindow popupwindow) {
        return popupwindow.I;
    }

    private int d(int i1) {
        int j1;
        j1 = 0xfff97de7 & i1;
        if (D) {
            j1 |= 0x8000;
        }
//        if (g)goto _L2;else goto _L1 _L1:
        j1 |= 8;
        if (h == 1) {
            j1 |= 0x20000;
        }
        _L4:
        if (!j) {
            j1 |= 0x10;
        }
        if (k) {
            j1 |= 0x40000;
        }
        if (!l) {
            j1 |= 0x200;
        }
        if (m) {
            j1 |= 0x100;
        }
        return j1;
//        _L2:
//        if (h == 2) {
//            j1 |= 0x20000;
//        }
//        if (true)goto _L4;else goto _L3 _L3:
    }

    static int d(PopupWindow popupwindow) {
        return popupwindow.J;
    }

    private void d(boolean flag) {
        if (flag != A) {
            A = flag;
            if (z != null) {
                f.refreshDrawableState();
            }
        }
    }

    static int[] d() {
        return F;
    }

    private int e() {
        if (E == -1) {
            if (d) {
                return !A
                       ? 0x7f0d008f
                       : 0x7f0d0090;
            } else {
                return 0;
            }
        } else {
            return E;
        }
    }

    static boolean e(PopupWindow popupwindow) {
        return popupwindow.A;
    }

    static View.OnTouchListener f(PopupWindow popupwindow) {
        return popupwindow.n;
    }

    private void f() {
        WeakReference weakreference = G;
        View view;
        if (weakreference != null) {
            view = (View) weakreference.get();
        } else {
            view = null;
        }
        if (view != null) {
            view.getViewTreeObserver()
                .removeOnScrollChangedListener(H);
        }
        G = null;
    }

    static View g(PopupWindow popupwindow) {
        return popupwindow.e;
    }

    public View a() {
        return e;
    }

    public void a(int i1) {
        E = i1;
    }

    public void a(int i1, int j1, int k1, int l1, boolean flag) {
        boolean flag1 = true;
        if (k1 != -1) {
            q = k1;
            c(k1);
        }
        if (l1 != -1) {
            t = l1;
            b(l1);
        }
        if (b() && e != null) {
            WindowManager.LayoutParams layoutparams = (WindowManager.LayoutParams) f.getLayoutParams();
            int i2;
            int j2;
            int k2;
            int l2;
            if (o < 0) {
                i2 = o;
            } else {
                i2 = q;
            }
            if (k1 != -1 && layoutparams.width != i2) {
                q = i2;
                layoutparams.width = i2;
                flag = flag1;
            }
            if (r < 0) {
                j2 = r;
            } else {
                j2 = t;
            }
            if (l1 != -1 && layoutparams.height != j2) {
                t = j2;
                layoutparams.height = j2;
                flag = flag1;
            }
            if (layoutparams.x != i1) {
                layoutparams.x = i1;
                flag = flag1;
            }
            if (layoutparams.y != j1) {
                layoutparams.y = j1;
                flag = flag1;
            }
            k2 = e();
            if (k2 != layoutparams.windowAnimations) {
                layoutparams.windowAnimations = k2;
                flag = flag1;
            }
            l2 = d(layoutparams.flags);
            if (l2 != layoutparams.flags) {
                layoutparams.flags = l2;
            } else {
                flag1 = flag;
            }
            if (flag1) {
                b.updateViewLayout(f, layoutparams);
                return;
            }
        }
    }

    public void a(Drawable drawable) {
        z = drawable;
    }

    public void a(View.OnTouchListener ontouchlistener) {
        n = ontouchlistener;
    }

    public void a(View view) {
        if (!b()) {
            e = view;
            if (a == null) {
                a = e.getContext();
            }
            if (b == null) {
                b = (WindowManager) a.getSystemService("window");
                return;
            }
        }
    }

    public void a(View view, int i1, int j1, int k1) {
        if (b() || e == null) {
            return;
        }
        f();
        c = true;
        d = false;
        WindowManager.LayoutParams layoutparams = a(view.getWindowToken());
        layoutparams.windowAnimations = e();
        a(layoutparams);
        if (i1 == 0) {
            i1 = 51;
        }
        layoutparams.gravity = i1;
        layoutparams.x = j1;
        layoutparams.y = k1;
        b(layoutparams);
    }

    public void a(boolean flag) {
        g = flag;
    }

    public void b(int i1) {
        s = i1;
    }

    public void b(boolean flag) {
        j = flag;
    }

    public boolean b() {
        return c;
    }

    public void c() {
//        if (!b() || f == null) {
//            break MISSING_BLOCK_LABEL_92;
//        }
//        f();
//        b.removeView(f);
//        if (f != e && (f instanceof ViewGroup)) {
//            ((ViewGroup) f).removeView(e);
//        }
//        f = null;
//        c = false;
//        if (C != null) {
//            C.a();
//        }
//        return;
//        Exception exception;
//        exception;
//        if (f != e && (f instanceof ViewGroup)) {
//            ((ViewGroup) f).removeView(e);
//        }
//        f = null;
//        c = false;
//        if (C != null) {
//            C.a();
//        }
//        throw exception;
    }

    public void c(int i1) {
        p = i1;
    }

    public void c(boolean flag) {
        k = flag;
    }


    private class PopupViewContainer extends FrameLayout {

        final PopupWindow a = PopupWindow.this;

        public PopupViewContainer(Context context) {
            this(context, null);
        }

        public PopupViewContainer(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public PopupViewContainer(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public boolean dispatchKeyEvent(KeyEvent keyevent) {
            if (keyevent.getKeyCode() == 4 && getKeyDispatcherState() != null) {
                if (keyevent.getAction() == 0 && keyevent.getRepeatCount() == 0) {
                    getKeyDispatcherState().startTracking(keyevent, this);
                    return true;
                }
                if (keyevent.getAction() == 1 && getKeyDispatcherState().isTracking(keyevent) && !keyevent.isCanceled()) {
                    a.c();
                    return true;
                } else {
                    return super.dispatchKeyEvent(keyevent);
                }
            } else {
                return super.dispatchKeyEvent(keyevent);
            }
        }

        public boolean dispatchTouchEvent(MotionEvent motionevent) {
            if (PopupWindow.f(a) != null && PopupWindow.f(a)
                                                       .onTouch(this, motionevent)) {
                return true;
            } else {
                return super.dispatchTouchEvent(motionevent);
            }
        }

        protected int[] onCreateDrawableState(int i1) {
            if (PopupWindow.e(a)) {
                int ai[] = super.onCreateDrawableState(i1 + 1);
                View.mergeDrawableStates(ai, PopupWindow.d());
                return ai;
            } else {
                return super.onCreateDrawableState(i1);
            }
        }

        public boolean onTouchEvent(MotionEvent motionevent) {
            int i1 = (int) motionevent.getX();
            int j1 = (int) motionevent.getY();
            if (motionevent.getAction() == 0 && (i1 < 0 || i1 >= super.getWidth() || j1 < 0 || j1 >= super.getHeight())) {
                a.c();
                return true;
            }
            if (motionevent.getAction() == 4) {
                a.c();
                return true;
            } else {
                return super.onTouchEvent(motionevent);
            }
        }

        public void sendAccessibilityEvent(int i1) {
            if (PopupWindow.g(a) != null) {
                PopupWindow.g(a)
                           .sendAccessibilityEvent(i1);
                return;
            } else {
                super.sendAccessibilityEvent(i1);
                return;
            }
        }
    }


    private interface OnDismissListener {

        public void a();
    }

}
