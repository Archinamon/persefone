package mobi.anoda.archinamon.kernel.persefone.ui.context;

import android.content.Context;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.delegate.NetworkState;
import mobi.anoda.archinamon.kernel.persefone.utils.Singleton;

/**
 * Created by matsukov-ea on 19.09.2014.
 */
public class StableContext extends AbstractStableContext {

    private static final Singleton<StableContext> gDefault = new Singleton<StableContext>() {

        @Implement
        protected StableContext create() {
            StableContext impl = new StableContext();
            impl.construct();
            return impl;
        }
    };

    private StableContext() {}

    public static <I extends Context> StableContext instantiate(I contextImpl) {
        StableContext impl = gDefault.getInstance();
        impl.update(impl, contextImpl);

        return impl;
    }

    public static StableContext obtain() {
        return gDefault.getInstance();
    }
}
