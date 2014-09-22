package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;

/**
 * Created by matsukov-ea on 22.09.2014.
 */
public final class DbLoader {

    protected     int                     mLoader;
    protected     LoaderCallbacks<Cursor> mLoaderCallbacks;
    private final StableContext           mStableContext;

    public DbLoader(StableContext stableContext) {
        this.mStableContext = stableContext;
    }

    public final void setLoaderId(int id) {
        this.mLoader = id;
    }

    public final void setCallbacks(LoaderCallbacks<Cursor> callbacks) {
        this.mLoaderCallbacks = callbacks;
    }

    public void onResume() {
        if (mLoaderCallbacks != null) {
            initLoader(mLoader, null, mLoaderCallbacks);
        }
    }

    protected Loader<Cursor> initLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        LoaderManager manager = mStableContext.obtainUiContext()
                                              .getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        } else {
            return manager.initLoader(id, params, mLoaderCallbacks);
        }
    }

    protected Loader<Cursor> restartLoader(int id, Bundle params, LoaderCallbacks<Cursor> mLoaderCallbacks) {
        LoaderManager manager = mStableContext.obtainUiContext()
                                              .getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(id);

        if (loader != null && !loader.isReset()) {
            return manager.restartLoader(id, params, mLoaderCallbacks);
        }

        return null;
    }
}
