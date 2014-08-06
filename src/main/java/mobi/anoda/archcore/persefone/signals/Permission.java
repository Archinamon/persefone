package mobi.anoda.archcore.persefone.signals;

/**
 * author: Archinamon project: Multi Locker
 */
public interface Permission {

    static String BASE     = "mobi.anoda.archcore.permission";
    static String KEY_DATA = ".key:data";

    String getPermission();
}
