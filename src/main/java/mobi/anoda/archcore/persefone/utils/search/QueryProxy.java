package mobi.anoda.archcore.persefone.utils.search;

import mobi.anoda.archcore.persefone.utils.AtomicString;

public final class QueryProxy {

    private final AtomicString mWatchedAtomic;
    private volatile boolean mIsMutable = true;

    QueryProxy(AtomicString watchedAtomic) {
        mWatchedAtomic = watchedAtomic;
    }

    public CharSequence getQuery() {
        return mWatchedAtomic.getQuery();
    }

    public void updateQuery(CharSequence value) {
        mWatchedAtomic.updateQuery(value);
    }

    public CharSequence getQueryAndUpdate(CharSequence value) {
        return mWatchedAtomic.getQueryAndUpdate(value);
    }

    public synchronized void setImmutable() {
        if (mIsMutable) {
            mIsMutable = false;

            if (mWatchedAtomic.isLocked()) {
                throw new IllegalStateException("Query already immutable");
            }

            mWatchedAtomic.lock();
        }
    }

    public synchronized void setMutable() {
        if (!mIsMutable) {
            mIsMutable = true;

            if (!mWatchedAtomic.isLocked()) {
                throw new IllegalStateException("Query already mutable");
            }

            mWatchedAtomic.unlock();
        }
    }
}