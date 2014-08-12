package mobi.anoda.archinamon.kernel.persefone.network.async;

import org.jetbrains.annotations.NotNull;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface APIErrorCode {

    int getMessage(@NotNull String code);
}
