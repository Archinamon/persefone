package mobi.anoda.archcore.persefone.signals;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface Broadcastable {

    static String BASE     = "mobi.anoda.archcore";
    static String TYPE     = ".action";
    static String KEY_DATA = ".key:data";

    String getAction();

    boolean isEqual(String with);
}
