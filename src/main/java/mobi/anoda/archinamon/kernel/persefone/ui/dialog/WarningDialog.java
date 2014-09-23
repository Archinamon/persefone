package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import android.content.DialogInterface.OnClickListener;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;

/**
 * author: Archinamon
 */
public class WarningDialog extends AbstractDialog {

    public static final String TAG = WarningDialog.class.getSimpleName();
    OnClickListener mOnClick;

    public WarningDialog() {
    }

    @Implement
    public String getViewTag() {
        return WarningDialog.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.ALERT);
        setTitle(Common.getApplicationName(StableContext.Impl.obtain().obtainAppContext()));
        setOkButton(R.string.popup_btn_ok, mOnClick);

        return true;
    }

    public void setOnClick(OnClickListener listener) {
        mOnClick = listener;
    }
}
