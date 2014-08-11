package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * @author archinamon
 */
public class ProgressPopup extends AbstractPopup {

    public static final String TAG = ProgressPopup.class.getSimpleName();

    public ProgressPopup() {}

    @Implement
    public String getViewTag() {
        return ProgressPopup.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.PROGRESS_SIMPLE);
        setMessage(R.string.loading);

        return true;
    }
}