package mobi.anoda.archcore.persefone.utils.search;

import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.utils.AtomicString;
import mobi.anoda.archcore.persefone.utils.WordUtils;

/**
 * author: Archinamon
 * project: FavorMe
 */
public final class TransientSearchImpl implements ITransientSearch {

    private static       QueryProxy sQueryProxy  = new QueryProxy(new AtomicString());
    private static final Object     STATIC_MUTEX = new Object();
    private static volatile TransientSearchImpl INSTANCE;

    public static TransientSearchImpl getInstance() {
        synchronized (STATIC_MUTEX) {
            if (INSTANCE == null) {
                INSTANCE = new TransientSearchImpl();
            }

            return INSTANCE;
        }
    }

    private static void initQuery(CharSequence value) {
        synchronized (STATIC_MUTEX) {
            if (sQueryProxy != null) {
                AtomicString aStr = WordUtils.isEmpty(value) ? new AtomicString() : new AtomicString(value);
                sQueryProxy = new QueryProxy(aStr);
            }
        }
    }

    private TransientSearchImpl() {}

    @Implement
    public QueryProxy obtainQueryProcessor() {
        synchronized (STATIC_MUTEX) {
            return sQueryProxy;
        }
    }

    @Implement
    public void initQueryProxy(CharSequence query) {
        TransientSearchImpl.initQuery(query);
    }

    public final void initQueryProxy() {
        TransientSearchImpl.initQuery("");
    }
}
