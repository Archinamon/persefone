package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * @author archinamon
 */
public class ProgressDialog extends AbstractDialog {

    public static final String TAG = ProgressDialog.class.getSimpleName();

    public ProgressDialog() {
    }

    @Implement
    public String getViewTag() {
        return ProgressDialog.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.PROGRESS_SIMPLE);
        setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        setMessage(R.string.loading);

        return true;
    }
}
