package mobi.anoda.archcore.persefone.ui.widget;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import mobi.anoda.archcore.persefone.R;

/**
 * @author: adnoda
 * @project: Facebook
 */
public class ActionUpdateButton {

    public static interface UpdateDelegate {

        void onProcess();

        void postResult();
    }

    public static final class Builder {

        MenuItem       mMenuItem;
        UpdateDelegate mCallback;
        int resId;

        public Builder(Menu menu) {
            this.resId = R.layout.action_progress;
            this.mMenuItem = menu.add(0, Menu.FIRST, 0, "");
        }

        public Builder setIcon(int icon) {
            this.mMenuItem.setIcon(icon);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.mMenuItem.setIcon(icon);
            return this;
        }

        public Builder setTitle(int title) {
            this.mMenuItem.setTitle(title);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mMenuItem.setTitle(title);
            return this;
        }

        public Builder setShowAsAction(int mode) {
            this.mMenuItem.setShowAsAction(mode);
            return this;
        }

        public Builder setVisible(boolean visible) {
            this.mMenuItem.setVisible(visible);
            return this;
        }

        public Builder setActionView(int resId) {
            this.resId = resId;
            return this;
        }

        public Builder setCallback(UpdateDelegate mCallback) {
            this.mCallback = mCallback;
            return this;
        }

        public ActionUpdateButton build() {
            return new ActionUpdateButton(mMenuItem, mCallback, resId);
        }
    }

    private class UpdateButtonTask extends AsyncTask<Void, Void, Void> {

        ActionUpdateButton mButton;

        UpdateButtonTask(ActionUpdateButton item) {
            this.mButton = item;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCallback.onProcess();
            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            mCallback.postResult();
            mButton.restore();
        }
    }

    private ActionUpdateButton(MenuItem mButton, UpdateDelegate mCallback, int resId) {
        this.mCallback = mCallback;
        this.mButton = mButton;
        this.resId = resId;
    }

    private MenuItem       mButton;
    private UpdateDelegate mCallback;
    private int            resId;

    public void onClick() {
        mButton.setActionView(resId);
        mButton.expandActionView();

        if (mCallback != null) {
            UpdateButtonTask updateButtonTask = new UpdateButtonTask(this);
            updateButtonTask.execute();
        }
    }

    public int getId() {
        return mButton.getItemId();
    }

    public void setTitle(String title) {
        mButton.setTitle(title);
    }

    public void setTitle(int titleId) {
        mButton.setTitle(titleId);
    }

    public void setIcon(int iconId) {
        mButton.setIcon(iconId);
    }

    public void setIcon(Drawable icon) {
        mButton.setIcon(icon);
    }

    public void collapseActionView() {
        restore();
    }

    private void restore() {
        mButton.collapseActionView();
        mButton.setActionView(null);
    }
}
