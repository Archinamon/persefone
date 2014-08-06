package mobi.anoda.archcore.persefone.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class CursorHelper {

    public static boolean getBoolean(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        return id != -1 && !cursor.isClosed() && cursor.getInt(id) > 0;
    }

    public static String getString(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id != -1 && !cursor.isClosed()) {
            return cursor.getString(id);
        }
        return null;
    }

    public static long getLong(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id != -1 && !cursor.isClosed()) {
            return cursor.getLong(id);
        }
        return -1;
    }

    public static int getInt(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id != -1 && !cursor.isClosed()) {
            return cursor.getInt(id);
        }
        return -1;
    }

    public static double getDouble(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id != -1 && !cursor.isClosed()) {
            return cursor.getDouble(id);
        }
        return -1;
    }

    public static void setBoolean( Cursor cursor, String column, boolean value, int id, Context context, Uri uri ) {
        String strFilter = "_id=" + id;
        ContentValues args = new ContentValues();
        args.put(column, value);
        context.getContentResolver().update(uri, args, strFilter, null);
    }

    public static void setLong(String column, long value, int id, Context context, Uri uri) {
        String strFilter = "_id=" + id;
        ContentValues args = new ContentValues();
        args.put(column, value);
        context.getContentResolver().update(uri, args, strFilter, null);
    }

    public static void setString( Cursor cursor, String column, String value, int id, Context context, Uri uri ) {
        String strFilter = "_id=" + id;
        ContentValues args = new ContentValues();
        args.put(column, value);
        context.getContentResolver().update(uri, args, strFilter, null);
    }
}