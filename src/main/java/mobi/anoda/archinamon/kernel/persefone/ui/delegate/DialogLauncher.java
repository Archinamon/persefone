package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;

/**
 * Created by matsukov-ea on 23.09.2014.
 */
public final class DialogLauncher {

    public static final String CUSTOM_DATA = ".ui:key_data";
    private final    StableContext mStableContext;
    private          Fragment      mFragment;
    private volatile boolean       isViaFragment;

    public DialogLauncher(@NonNull StableContext stableContext) {
        this.mStableContext = stableContext;
        this.isViaFragment = false;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
        this.isViaFragment = true;
    }

    /* Launch Popup dialog */
    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass) {
        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, null);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, Bundle params) {
        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, Parcelable data) {
        final Bundle params = new Bundle();
        params.putParcelable(CUSTOM_DATA, data);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractDialog.IEXTRA_MESSAGE, message);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    public AbstractDialog openPopup(Class<? extends AbstractDialog> dialogClass, String title, String message) {
        final Bundle params = new Bundle();
        params.putString(AbstractDialog.IEXTRA_TITLE, title);
        params.putString(AbstractDialog.IEXTRA_MESSAGE, message);

        AbstractDialog dialog = AbstractDialog.newInstance(dialogClass, params);
        return openPopupInternal(dialog);
    }

    private AbstractDialog openPopupInternal(AbstractDialog popup) {
        final String tag = popup.getViewTag();
        FragmentManager manager = obtainFragmentManager();

        if (!popup.isShowing(tag)) {
            popup.show(manager, tag);
        }

        return popup;
    }

    private FragmentManager obtainFragmentManager() {
        if (isViaFragment) return mFragment.getFragmentManager();
        else {
            final AbstractActivity ui = mStableContext.obtainUiContext();
            return ui.getSupportFragmentManager();
        }
    }
}
