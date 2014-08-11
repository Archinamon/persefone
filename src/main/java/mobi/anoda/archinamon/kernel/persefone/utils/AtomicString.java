package mobi.anoda.archinamon.kernel.persefone.utils;

import android.util.Log;
import java.io.Serializable;

/**
 * author: Archinamon
 * project: FavorMe
 */
public final class AtomicString implements Serializable {

    public static final  String  TAG              = AtomicString.class.getSimpleName();
    private static final long    serialVersionUID = -1848883925212044542L;
    private final        Object  MUTEX            = new Object();
    private volatile     boolean mIsMutable       = true;
    private volatile String  mValue;

    public AtomicString() {
        mValue = "";
    }

    public AtomicString(CharSequence initValue) {
        mValue = initValue.toString();
    }

    public final boolean isLocked() {
        return !mIsMutable;
    }

    public final synchronized void lock() {
        mIsMutable = false;

        Log.w(TAG, "Locked");
    }

    public final synchronized void unlock() {
        mIsMutable = true;

        Log.w(TAG, "Unlocked");
    }

    public final void updateQuery(CharSequence value) {
        synchronized (MUTEX) {
            if (mIsMutable)
                mValue = value.toString();
        }

        Log.w(TAG, "isMutable: " + mIsMutable);
        Log.w(TAG, "Query: " + value);
    }

    public final CharSequence getQuery() {
        return mValue;
    }

    public final CharSequence getQueryAndUpdate(CharSequence value) {
        CharSequence old;
        synchronized (MUTEX) {
            old = mValue.intern();
            if (mIsMutable)
                mValue = value.toString();
        }

        return old;
    }
}
