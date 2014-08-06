package mobi.anoda.archcore.persefone.network.processor;

import mobi.anoda.archcore.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archcore.persefone.ui.activity.AbstractActivity;

/**
 * This class delegates basic strategies of server interruption commands
 *
 * @author: Archinamon
 * @project: FavorMe
 * @hide
 */
public interface IStrategyInterrupt {

    void jump(AbstractActivity context, Class<? extends AbstractActivity> lngjump_to);

    void fatal(Throwable fatalExc);

    void operate(ErrorReport report);
}
