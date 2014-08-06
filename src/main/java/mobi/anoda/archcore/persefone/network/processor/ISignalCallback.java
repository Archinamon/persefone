package mobi.anoda.archcore.persefone.network.processor;

import mobi.anoda.archcore.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archcore.persefone.signals.Channel;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignalCallback {

    void $(Channel gate, ErrorReport report);
}
