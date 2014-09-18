package mobi.anoda.archinamon.kernel.persefone.ui.activity;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import mobi.anoda.archinamon.kernel.persefone.ui.AbstractStableContext;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
public final class SoftKeyboardDelegate {

    private final AbstractStableContext mStableContext;
    private final InputMethodManager    mInputManagerService;

    public SoftKeyboardDelegate(AbstractStableContext stableContext) {
        this.mStableContext = stableContext;
        this.mInputManagerService = (InputMethodManager) stableContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /* Helper to hide soft input KeyboardView */
    public void hideSoftInput(final int view) {
        final View viewInstance = mStableContext.findViewById(view);

        if (viewInstance != null) mInputManagerService.hideSoftInputFromWindow(viewInstance.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void hideSoftInput(final View view) {
        mInputManagerService.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /* Helper to show soft input KeyboardView */
    public void showSoftInput(View v) {
        mInputManagerService.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
}
