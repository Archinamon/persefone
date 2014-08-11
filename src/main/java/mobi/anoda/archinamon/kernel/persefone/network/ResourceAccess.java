package mobi.anoda.archinamon.kernel.persefone.network;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ResourceAccess {

    String getString(int resId);

    String getString(int resId, Object... modifiers);
}
