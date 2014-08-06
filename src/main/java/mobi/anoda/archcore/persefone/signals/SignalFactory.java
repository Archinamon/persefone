package mobi.anoda.archcore.persefone.signals;

import mobi.anoda.archcore.persefone.network.processor.ISignal;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
@SuppressWarnings("FinalStaticMethod")
public final class SignalFactory {

    public static final String TAG = SignalFactory.class.getSimpleName();

    public static final ISignal createSignal(String actionCommand) {
        return new SignalImpl(actionCommand);
    }

    public static final ISignal createSignal(Broadcastable actionCommand) {
        return new SignalImpl(actionCommand);
    }
}
