package mobi.anoda.archinamon.kernel.persefone.ui.utils;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import java.util.HashMap;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
 * @author archinamon
 */
public class FragmentTabListener implements ActionBar.TabListener {

    private class TabContent {

        private Bundle mBundle;
        private Fragment mFragment;
        private Class mClass;

        public boolean isAcivated() {
            return mFragment != null;
        }

        public Fragment getFragment() {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mContext, mClass.getName(), mBundle);
            }
            return mFragment;
        }
    }

    public static final String                             TAG   = FragmentTabListener.class.getSimpleName();
    private             HashMap<ActionBar.Tab, TabContent> mTabs = new HashMap<>();
    private Context         mContext;
    private ActionBar       mActionBar;
    private FragmentManager mFragmentManager;
    private boolean isRecreated = false;

    public FragmentTabListener(Context context, ActionBar actionBar, FragmentManager manager, boolean isRecreated) {
        mContext = context;
        mActionBar = actionBar;
        mFragmentManager = manager;
        this.isRecreated = isRecreated;
    }

    public void addTab(ActionBar.Tab tab, Class clazz, Bundle params, String title) {
        TabContent tabContent = new TabContent();
        tabContent.mClass = clazz;
        tabContent.mBundle = params;
        tab.setTabListener(this);
        tab.setText(title);
        mTabs.put(tab, tabContent);
        mActionBar.addTab(tab);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        TabContent content = mTabs.get(tab);
        if (!isRecreated) {
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (!content.isAcivated()) {
                ft.add(R.id.fragment_view,
                       content.getFragment(),
                       content.mClass.getSimpleName());
            } else {
                Fragment f = content.getFragment();
                f.onResume();
                ft.attach(f);
            }
        }
        isRecreated = false;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        TabContent content = mTabs.get(tab);
        ft.detach(content.getFragment());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        onTabSelected(tab, ft);
    }

    public AbstractFragment getCurrentTabFragment(ActionBar.Tab tab) {
        return mTabs.containsKey(tab) ? (AbstractFragment) mTabs.get(tab).getFragment() : null;
    }
}
