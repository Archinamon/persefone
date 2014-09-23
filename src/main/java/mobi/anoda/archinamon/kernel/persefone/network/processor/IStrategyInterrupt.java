package mobi.anoda.archinamon.kernel.persefone.network.processor;

import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperation.ErrorReport;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;

/**
 * This class delegates basic strategies of server interruption commands
 *
 * @author: Archinamon
 * @project: FavorMe
 * @hide
 */
public interface IStrategyInterrupt {

    void jump(StableContext context, Class<? extends AbstractActivity> lngjump_to);

    void fatal(Throwable fatalExc);

    void operate(ErrorReport report);
}
