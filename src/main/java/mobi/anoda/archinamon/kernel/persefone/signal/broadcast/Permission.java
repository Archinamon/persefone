package mobi.anoda.archinamon.kernel.persefone.signal.broadcast;

/**
 * author: Archinamon project: Multi Locker
 */
public interface Permission {

    static String BASE     = "mobi.anoda.archcore.permission";
    static String KEY_DATA = ".key:data";

    String getPermission();
}
