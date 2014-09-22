package mobi.anoda.archinamon.kernel.persefone.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.intellij.lang.annotations.MagicConstant;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.interfaces.StateControllable;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbsSwyperFragment;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbsSwyperListFragment;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbstractDrawerActivity<S extends AbstractFragment & StateControllable> extends AbstractActivity<S> {

    protected DrawerLayout          mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    private   int                   mDrawerGravity;

    protected final void initDrawer(@MagicConstant(flagsFromClass = Gravity.class) int gravity) {
        mDrawerGravity = gravity;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(mSelf, mDrawerLayout, R.drawable.navbar_btn_menu, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                getKeyboardDelegate().hideSoftInput(mDrawerLayout);
                openDrawerCallback();
            }

            @Override
            public void onDrawerClosed(View view) {
                closeDrawerCallback();
            }

            private void openDrawerCallback() {
                FragmentManager manager = getSupportFragmentManager();
                AbstractFragment swipeMenu = (AbstractFragment) manager.findFragmentByTag("swipe_menu");
                if (swipeMenu instanceof AbsSwyperListFragment) {
                    AbsSwyperListFragment.riseOnOpenDrawer((AbsSwyperListFragment) swipeMenu, true);
                } else {
                    AbsSwyperFragment.riseOnOpenDrawer((AbsSwyperFragment) swipeMenu, true);
                }
            }

            private void closeDrawerCallback() {
                FragmentManager manager = getSupportFragmentManager();
                AbstractFragment swipeMenu = (AbstractFragment) manager.findFragmentByTag("swipe_menu");
                if (swipeMenu instanceof AbsSwyperListFragment) {
                    AbsSwyperListFragment.riseOnCloseDrawer((AbsSwyperListFragment) swipeMenu, true);
                } else {
                    AbsSwyperFragment.riseOnCloseDrawer((AbsSwyperFragment) swipeMenu, true);
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        closeDrawerMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean parent = super.onPrepareOptionsMenu(menu);

        if (mDrawerLayout != null) {
            return !mDrawerLayout.isDrawerVisible(mDrawerGravity);
        } else {
            return parent;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null) {
            return mDrawerToggle.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerGravity))
            closeDrawerMenu();
        else super.onBackPressed();
    }

    public void toggleDrawerMenu() {
        if (mDrawerLayout.isDrawerOpen(mDrawerGravity)) closeDrawerMenu();
        else openDrawerMenu();
    }

    public void openDrawerMenu() {
        if (mDrawerLayout != null) mDrawerLayout.openDrawer(mDrawerGravity);
    }

    public void closeDrawerMenu() {
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mDrawerGravity);
    }
}
