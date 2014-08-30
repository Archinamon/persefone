package mobi.anoda.archinamon.kernel.persefone.ui.fragment.popup;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {

    private Drawable        mActionImg;
    private String          mActionTitle;
    private OnClickListener mCallback;

    public ActionItem(String title, Drawable icon) {
        mActionTitle = title;
        mActionImg = icon;
    }

    public String getTitle() {
        return mActionTitle;
    }

    public void setIcon(Drawable drawable) {
        mActionImg = drawable;
    }

    public void setCallback(OnClickListener onclicklistener) {
        mCallback = onclicklistener;
    }

    public void setTitle(String s) {
        mActionTitle = s;
    }

    public Drawable getIcon() {
        return mActionImg;
    }

    public OnClickListener getCallback() {
        return mCallback;
    }
}
