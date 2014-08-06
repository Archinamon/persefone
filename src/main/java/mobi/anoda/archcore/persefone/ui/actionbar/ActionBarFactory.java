package mobi.anoda.archcore.persefone.ui.actionbar;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.SpinnerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mobi.anoda.archcore.persefone.ui.fragment.AbstractFragment;
import mobi.anoda.archcore.persefone.ui.utils.FragmentTabListener;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
// chained factory
public final class ActionBarFactory {

    private final Object MUTEX = new Object();
    @NotNull
    private final ActionBar        fBarInstance;
    private       ActionBarPattern mPattern;

    public ActionBarFactory(@Nullable final ActionBar inst) throws IllegalAccessException {
        if (inst == null) {
            throw new IllegalAccessException("trying to setup an ActionBarFactory without active ActionBar for current activity");
        }

        this.fBarInstance = inst;
        this.fBarInstance.show();
    }

    public final synchronized ActionBar build() {
        return this.fBarInstance;
    }

    public ActionBarFactory applyDefaults() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.DEFAULT;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyCustomTitle() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.CUSTOM_TITLE;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyCustomLogo() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.CUSTOM_LOGO;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyStandardUp() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.DEFAULT_UP;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyNoLogo() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.NO_LOGO;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyTabbedNavigator() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.NAVIGATION_TABS;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyListNavigator() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.NAVIGATION_LIST;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyDrawerNavigator() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.NAVIGATION_DRAWER;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory applyCustomNavigator() {
        synchronized (MUTEX) {
            this.mPattern = ActionBarPattern.NAVIGATION_CUSTOM;
        }

        try {
            innerApplyPattern();
        } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
        }

        return this;
    }

    public ActionBarFactory withTitle(int resId) {
        this.fBarInstance.setTitle(resId);
        return this;
    }

    public ActionBarFactory withTitle(CharSequence title) {
        this.fBarInstance.setTitle(title);
        return this;
    }

    public ActionBarFactory withSubTitle(int resId) {
        this.fBarInstance.setSubtitle(resId);
        return this;
    }

    public ActionBarFactory withSubTitle(CharSequence title) {
        this.fBarInstance.setSubtitle(title);
        return this;
    }

    public ActionBarFactory withLogo(int resId) {
        this.fBarInstance.setLogo(resId);
        return this;
    }

    public ActionBarFactory withLogo(Drawable img) {
        this.fBarInstance.setLogo(img);
        return this;
    }

    public ActionBarFactory withView(int resId) {
        this.fBarInstance.setCustomView(resId);
        return this;
    }

    public ActionBarFactory withView(View view) {
        this.fBarInstance.setCustomView(view);
        return this;
    }

    public ActionBarFactory withView(View view, ActionBar.LayoutParams params) {
        this.fBarInstance.setCustomView(view, params);
        return this;
    }

    public ActionBarFactory withListListener(SpinnerAdapter adapter, ActionBar.OnNavigationListener listener) {
        this.fBarInstance.setListNavigationCallbacks(adapter, listener);
        return this;
    }

    public ActionBarFactory withTabs(FragmentTabListener tabHost, final Class<? extends AbstractFragment>[] fragments, String[] titles, Bundle[] params) {
        if (tabHost != null) {
            initTabs(tabHost, fragments, titles, params);
        }

        return this;
    }

    public ActionBarFactory withTabs(FragmentTabListener tabHost, final Class<? extends AbstractFragment>[] fragments, String[] titles) {
        if (tabHost != null) {
            initTabs(tabHost, fragments, titles, null);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, final View... views) {
        for (View v : views) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setCustomView(v);
            tab.setTag(v.getTag());
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, final String[] titles) {
        for (String str : titles) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(str);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, final int[] titles) {
        for (int titleId : titles) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(titleId);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, String[] titles, int[] icons) {
        for (int i = 0; i < titles.length; i++) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(titles[i]);
            tab.setIcon(icons[i]);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, int[] titles, int[] icons) {
        for (int i = 0; i < titles.length; i++) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(titles[i]);
            tab.setIcon(icons[i]);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, String[] titles, Drawable[] icons) {
        for (int i = 0; i < titles.length; i++) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(titles[i]);
            tab.setIcon(icons[i]);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory withTabs(TabListener listener, int[] titles, Drawable[] icons) {
        for (int i = 0; i < titles.length; i++) {
            ActionBar.Tab tab = this.fBarInstance.newTab();

            tab.setText(titles[i]);
            tab.setIcon(icons[i]);
            tab.setTabListener(listener);

            this.fBarInstance.addTab(tab);
        }

        return this;
    }

    public ActionBarFactory resetNavigator() {
        fBarInstance.removeAllTabs();
        fBarInstance.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        return this;
    }

    private void initTabs(@NotNull final FragmentTabListener tabHost, final Class<? extends AbstractFragment>[] fragments, final String[] titles, Bundle[] params) {
        for (int p = 0; p < titles.length; p++) {
            ActionBar.Tab tab = this.fBarInstance.newTab();
            tabHost.addTab(tab,
                           fragments[p],
                           params != null ? params[p] : null,
                           titles[p]);
        }
    }

    /**
     * remember to avoid uncomplete statements
     * only one state enabled at once
     * new state will elliminate previous
     */
    private void innerApplyPattern() throws IllegalAccessException {
        if (this.mPattern == null) {
            mPattern = ActionBarPattern.DEFAULT;
        }

        switch (mPattern) {
            case DEFAULT:
                fBarInstance.setDisplayShowCustomEnabled(false);
                fBarInstance.setDisplayShowTitleEnabled(true);
                fBarInstance.setDisplayHomeAsUpEnabled(false);
                fBarInstance.setDisplayShowHomeEnabled(true);
                fBarInstance.setHomeButtonEnabled(true);
                break;
            case CUSTOM_TITLE:
                fBarInstance.setDisplayShowTitleEnabled(false);
                fBarInstance.setDisplayShowCustomEnabled(true);
                break;
            case CUSTOM_LOGO:
                fBarInstance.setDisplayUseLogoEnabled(false);
                fBarInstance.setDisplayHomeAsUpEnabled(false);
                fBarInstance.setDisplayShowTitleEnabled(false);
                fBarInstance.setDisplayShowCustomEnabled(true);
                break;
            case DEFAULT_UP:
                fBarInstance.setDisplayHomeAsUpEnabled(true);
                fBarInstance.setDisplayShowHomeEnabled(true);
                break;
            case NO_LOGO:
                fBarInstance.setDisplayHomeAsUpEnabled(false);
                fBarInstance.setDisplayShowHomeEnabled(true);
                fBarInstance.setHomeButtonEnabled(true);
                break;
            case NAVIGATION_TABS:
                fBarInstance.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                fBarInstance.setHomeButtonEnabled(true);
                break;
            case NAVIGATION_LIST:
                fBarInstance.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                fBarInstance.setHomeButtonEnabled(true);
                break;
            case NAVIGATION_DRAWER:
                fBarInstance.setDisplayHomeAsUpEnabled(true);
                fBarInstance.setHomeButtonEnabled(true);
                break;
            case NAVIGATION_CUSTOM:
                fBarInstance.removeAllTabs();
                fBarInstance.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fBarInstance.setDisplayShowCustomEnabled(true);
                break;
        }
    }
}