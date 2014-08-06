package mobi.anoda.archcore.persefone.ui.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.collect.ImmutableList;
import java.util.List;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.ui.fragment.AbstractFragment;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class FragmentPagerAdapter extends PagerAdapter {

    public static final String TAG = FragmentPagerAdapter.class.getSimpleName();
    private final ImmutableList<AbstractFragment> mFragmentsContainer;
    private final ImmutableList<String>           mElementTitles;
    private final FragmentManager                 mFragmentManager;
    private FragmentTransaction mCurTransaction     = null;
    private Fragment            mCurrentPrimaryItem = null;

    public FragmentPagerAdapter(Activity act, List<? extends AbstractFragment> objects) {
        mFragmentManager = act.getFragmentManager();
        mFragmentsContainer = ImmutableList.copyOf(objects);
        mElementTitles = null;
    }

    public FragmentPagerAdapter(Activity act, List<? extends AbstractFragment> objects, String[] titles) {
        mFragmentManager = act.getFragmentManager();
        mFragmentsContainer = ImmutableList.copyOf(objects);
        mElementTitles = ImmutableList.copyOf(titles);
    }

    public AbstractFragment getItem(int position) {
        return mFragmentsContainer.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mElementTitles != null
               ? mElementTitles.get(position)
               : "";
    }

    @Implement
    public int getCount() {
        return mFragmentsContainer.size();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final long itemId = getItemId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(container.getId(), itemId);
        AbstractFragment fragment = (AbstractFragment) mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                                makeFragmentName(container.getId(), itemId));
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return fragment;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        mCurTransaction.detach((Fragment)object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        AbstractFragment fragment = (AbstractFragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }

            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }

            mCurrentPrimaryItem = fragment;
        }
    }

    @Implement
    public boolean isViewFromObject(View view, Object o) {
        return ((Fragment) o).getView() == view;
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
