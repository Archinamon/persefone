package mobi.anoda.archinamon.kernel.persefone.network.processor;

import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.signal.impl.ServiceChannel;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignalCallback {

    void $(ServiceChannel gate, ErrorReport report);
}
