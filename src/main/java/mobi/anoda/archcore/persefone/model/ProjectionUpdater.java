package mobi.anoda.archcore.persefone.model;

import android.content.ContentValues;
import android.content.Context;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ProjectionUpdater {

    ContentValues MODEL_CONTENT = new ContentValues();

    boolean updateDB(Context c);
}
