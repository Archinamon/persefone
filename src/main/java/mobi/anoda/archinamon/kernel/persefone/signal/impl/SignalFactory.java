package mobi.anoda.archinamon.kernel.persefone.signal.impl;

import mobi.anoda.archinamon.kernel.persefone.network.processor.ISignal;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
@SuppressWarnings("FinalStaticMethod")
public final class SignalFactory {

    public static final String TAG = SignalFactory.class.getSimpleName();

    public static final ISignal createSignal(String actionCommand) {
        return new ServiceSignal(actionCommand);
    }

    public static final ISignal createSignal(Broadcastable actionCommand) {
        return new ServiceSignal(actionCommand);
    }
}
