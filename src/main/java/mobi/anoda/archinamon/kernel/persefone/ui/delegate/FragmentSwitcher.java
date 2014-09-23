package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
 * Created by matsukov-ea on 23.09.2014.
 */
public final class FragmentSwitcher {

    public static final String CUSTOM_DATA = ".ui:key_data";
    private final StableContext mStableContext;
    private Fragment mFragment;
    private volatile boolean isViaFragment;

    public FragmentSwitcher(@NonNull StableContext stableContext) {
        this.mStableContext = stableContext;
        this.isViaFragment = false;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
        this.isViaFragment = true;
    }

    public final void stepOutFragment() {
        final AbstractActivity ui = mStableContext.obtainUiContext();
        FragmentManager manager = ui.getSupportFragmentManager();
        if (!manager.popBackStackImmediate()) {
            ui.finish();
        }
    }

    /* Helper for opening new fragment */
    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, null);

        switchFragmentInternal(fragment, addToStack);

        return fragment;
    }

    public AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, Bundle params, boolean addToStack) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, addToStack);

        return fragment;
    }

    protected AbstractFragment switchFragment(Class<? extends AbstractFragment> fragmentClass, Bundle params) {
        AbstractFragment fragment = AbstractFragment.newInstance(fragmentClass, params);

        switchFragmentInternal(fragment, true);

        return fragment;
    }

    private void switchFragmentInternal(AbstractFragment fragment, boolean isAddingToBackstack) {
        final String tag = fragment.getViewTag();
        FragmentManager manager = obtainFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (isAddingToBackstack) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            }
            transaction.addToBackStack(tag);
        }

        transaction.replace(R.id.fragment_view, fragment, tag);
        transaction.commit();
    }

    private FragmentManager obtainFragmentManager() {
        if (isViaFragment) return mFragment.getFragmentManager();
        else {
            final AbstractActivity ui = mStableContext.obtainUiContext();
            return ui.getSupportFragmentManager();
        }
    }
}
