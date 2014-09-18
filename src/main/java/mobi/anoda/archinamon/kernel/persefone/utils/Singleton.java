package mobi.anoda.archinamon.kernel.persefone.utils;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
public abstract class Singleton<O> {

    private static final Object STATIC_LOCK = new Object();
    private O sInstance;

    protected abstract O create();

    public O getInstance() {
        if (sInstance == null) {
            synchronized (STATIC_LOCK) {
                sInstance = this.create();
            }
        }

        return this.sInstance;
    }
}
