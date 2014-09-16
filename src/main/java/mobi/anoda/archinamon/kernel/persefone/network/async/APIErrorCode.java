package mobi.anoda.archinamon.kernel.persefone.network.async;

import javax.annotation.Nonnull;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface APIErrorCode {

    int getMessage(@Nonnull String code);
}
