package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import android.content.DialogInterface.OnClickListener;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;

/**
 * author: Archinamon
 */
public class WarningPopup extends AbstractPopup {

    public static final String TAG = WarningPopup.class.getSimpleName();
    OnClickListener mOnClick;

    public WarningPopup() {
    }

    @Implement
    public String getViewTag() {
        return WarningPopup.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.ALERT);
        setTitle(Common.getApplicationName(mContext));
        setOkButton(R.string.popup_btn_ok, mOnClick);

        return true;
    }

    public void setOnClick(OnClickListener listener) {
        mOnClick = listener;
    }
}
