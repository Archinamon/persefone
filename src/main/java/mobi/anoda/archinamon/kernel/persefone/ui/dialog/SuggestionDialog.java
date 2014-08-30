package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class SuggestionDialog extends AbstractDialog {

    public static final String TAG = SuggestionDialog.class.getSimpleName();

    public SuggestionDialog() {
    }

    @Implement
    public String getViewTag() {
        return SuggestionDialog.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.PROMPT_YES_NO);
        setTitle(Common.getApplicationName(mContext));

        return true;
    }
}
