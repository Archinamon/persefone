package mobi.anoda.archinamon.kernel.persefone.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class CardLayout extends FrameLayout {

    private int mSelectedId;

    public CardLayout(Context context) {
        super(context);
    }

    public CardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void select(@IdRes int id) {
        mSelectedId = id;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View view = getChildAt(i);
            if (view.getId() == id) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public int getSelectedId() {
        return mSelectedId;
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt(String.valueOf(getId()), mSelectedId);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            final int selectedId = bundle.getInt(String.valueOf(getId()), -1);
            if (selectedId > 0) {
                select(selectedId);
            }
        }
    }
}