package mobi.anoda.archinamon.kernel.persefone.signal.broadcast;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface Broadcastable {

    static String BASE     = "mobi.anoda.archinamon.kernel";
    static String TYPE     = ".action";
    static String KEY_DATA = ".key:data";

    String getAction();

    boolean isEqual(String with);
}
