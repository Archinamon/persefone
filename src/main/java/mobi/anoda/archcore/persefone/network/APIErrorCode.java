package mobi.anoda.archcore.persefone.network;

import org.jetbrains.annotations.NotNull;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface APIErrorCode {

    int getMessage(@NotNull String code);
}
