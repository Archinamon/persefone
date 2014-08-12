package mobi.anoda.archinamon.kernel.persefone.network.async;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ResourceAccess {

    String getString(int resId);

    String getString(int resId, Object... modifiers);
}
