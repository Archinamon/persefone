package mobi.anoda.archinamon.kernel.persefone.ui.fragment.popup;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import mobi.anoda.archinamon.kernel.persefone.R;

public class QuickAction extends CustomPopupWindow {

    private final View           d;
    private final ImageView      e;
    private final ImageView      f;
    private final Animation      g;
    private final LayoutInflater h;
    private final Context        i;
    private       int            j;
    private       boolean        k;
    private       ViewGroup      l;
    private       ArrayList      m;

    public QuickAction(View view) {
        super(view);
        m = new ArrayList();
        i = view.getContext();
        h = (LayoutInflater) i.getSystemService("layout_inflater");
        d = (ViewGroup) h.inflate(R.layout.quickaction, null);
        f = (ImageView) d.findViewById(R.id.arrow_down);
        e = (ImageView) d.findViewById(R.id.arrow_up);
        build(d);
        g = AnimationUtils.loadAnimation(view.getContext(), 0x7f04000d);
        g.setInterpolator(new Interpolator() {

            final QuickAction a = QuickAction.this;

            public float getInterpolation(float f1) {
                float f2 = 1.55F * f1 - 1.1F;
                return 1.2F - f2 * f2;
            }
        });
        l = (ViewGroup) d.findViewById(0x7f07014b);
        j = 4;
        k = true;
    }

    private View a(String s, Drawable drawable, View.OnClickListener onclicklistener) {
        LinearLayout linearlayout = (LinearLayout) h.inflate(0x7f030016, null);
        ImageView imageview = (ImageView) linearlayout.findViewById(0x7f07005e);
        TextView textview = (TextView) linearlayout.findViewById(0x7f07005f);
        if (drawable != null) {
            imageview.setImageDrawable(drawable);
        } else {
            imageview.setVisibility(8);
        }
        if (s != null) {
            textview.setText(s);
        } else {
            textview.setVisibility(8);
        }
        if (onclicklistener != null) {
            linearlayout.setOnClickListener(onclicklistener);
        }
        return linearlayout;
    }

    private void a(int i1, int j1) {
        ImageView imageview;
        ImageView imageview1;
        int k1;
        if (i1 == 0x7f070149) {
            imageview = e;
        } else {
            imageview = f;
        }
        if (i1 == 0x7f070149) {
            imageview1 = f;
        } else {
            imageview1 = e;
        }
        k1 = e.getMeasuredWidth();
        imageview.setVisibility(0);
        ((ViewGroup.MarginLayoutParams) imageview.getLayoutParams()).leftMargin = j1 - k1 / 2;
        imageview1.setVisibility(8);
    }

    private void a(int i1, int j1, boolean flag) {
        int k1 = 0x7f0d00ba;
        int l1 = 0x7f0d00b8;
        int i2 = j1 - e.getMeasuredWidth() / 2;
        switch (j) {
            default:
                return;

            case 1: // '\001'
                PopupWindow popupwindow4 = mPopupFrame;
                if (!flag) {
                    l1 = 0x7f0d00b4;
                }
                popupwindow4.a(l1);
                return;

            case 2: // '\002'
                PopupWindow popupwindow3 = mPopupFrame;
                int k2;
                if (flag) {
                    k2 = 0x7f0d00b9;
                } else {
                    k2 = 0x7f0d00b5;
                }
                popupwindow3.a(k2);
                return;

            case 3: // '\003'
                PopupWindow popupwindow2 = mPopupFrame;
                int j2;
                if (flag) {
                    j2 = k1;
                } else {
                    j2 = 0x7f0d00b6;
                }
                popupwindow2.a(j2);
                return;

            case 4: // '\004'
                break;
        }
        if (i2 <= i1 / 4) {
            PopupWindow popupwindow1 = mPopupFrame;
            if (!flag) {
                l1 = 0x7f0d00b4;
            }
            popupwindow1.a(l1);
            return;
        }
        if (i2 > i1 / 4 && i2 < 3 * (i1 / 4)) {
            PopupWindow popupwindow = mPopupFrame;
            if (!flag) {
                k1 = 0x7f0d00b6;
            }
            popupwindow.a(k1);
            return;
        } else {
            mPopupFrame.a(0x7f0d00b5);
            return;
        }
    }

    private void f() {
        int i1 = 0;
        int j1 = 1;
        do {
            if (i1 >= m.size()) {
                return;
            }
            View view = a(((ActionItem) m.get(i1)).getTitle(), ((ActionItem) m.get(i1)).getIcon(), ((ActionItem) m.get(i1)).getCallback());
            view.setFocusable(true);
            view.setClickable(true);
            l.addView(view, j1);
            j1++;
            i1++;
        } while (true);
    }

    public void a(int i1) {
        j = i1;
    }

    public void a(ActionItem actionitem) {
        m.add(actionitem);
    }

    public void e() {
        f();
        show();
        int ai[] = new int[2];
        mActionView.getLocationOnScreen(ai);
        Rect rect = new Rect(ai[0], ai[1], ai[0] + mActionView.getWidth(), ai[1] + mActionView.getHeight());
        d.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        d.measure(View.MeasureSpec.makeMeasureSpec(-2, 0x80000000), View.MeasureSpec.makeMeasureSpec(-2, 0x80000000));
        int i1 = l.getMeasuredWidth();
        int j1 = l.getMeasuredHeight();
        View view = d.findViewById(0x7f07014a);
        view.setLayoutParams(new LinearLayout.LayoutParams(i1, view.getLayoutParams().height));
        View view1 = d.findViewById(0x7f070079);
        view1.setLayoutParams(new LinearLayout.LayoutParams(i1, view1.getLayoutParams().height));
        int k1 = mActionView.getWidth();
        int l1 = rect.left + (k1 - i1) / 2;
        int i2 = rect.top - j1;
        int j2;
        boolean flag;
        int k2;
        if (j1 > mActionView.getTop()) {
            int l2 = rect.bottom;
            flag = false;
            j2 = l2;
        } else {
            j2 = i2;
            flag = true;
        }
        if (flag) {
            k2 = 0x7f07014c;
        } else {
            k2 = 0x7f070149;
        }
        a(k2, i1 / 2);
        a(k1, rect.centerX(), flag);
        mPopupFrame.a(mActionView, 0, l1, j2);
        if (k) {
            l.startAnimation(g);
        }
    }
}
