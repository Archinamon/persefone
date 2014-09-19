package mobi.anoda.archinamon.kernel.persefone.signal.event;

import com.squareup.otto.Bus;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.Singleton;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
public final class EventBus {

    private static final Singleton<Bus> gDefault = new Singleton<Bus>() {

        @Implement
        protected final Bus create() {
            return new Bus();
        }
    };

    public static Bus instantiate() {
        return gDefault.getInstance();
    }
}
