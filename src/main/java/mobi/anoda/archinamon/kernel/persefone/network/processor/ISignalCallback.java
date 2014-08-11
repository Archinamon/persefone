package mobi.anoda.archinamon.kernel.persefone.network.processor;

import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.signals.Channel;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignalCallback {

    void $(Channel gate, ErrorReport report);
}
