package mobi.anoda.archcore.persefone.ui.dialog;

import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.utils.Common;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class SuggestionPopup extends AbstractPopup {

    public static final String TAG = SuggestionPopup.class.getSimpleName();

    public SuggestionPopup() {}

    @Implement
    public String getViewTag() {
        return SuggestionPopup.TAG;
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.PROMPT_YES_NO);
        setTitle(Common.getApplicationName(mContext));

        return true;
    }
}
