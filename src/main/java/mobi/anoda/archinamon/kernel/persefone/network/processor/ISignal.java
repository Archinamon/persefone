package mobi.anoda.archinamon.kernel.persefone.network.processor;

import android.os.Parcelable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignal extends Parcelable  {

    static String BASE     = "mobi.anoda.archcore.signal";
    static String KEY_DATA = ".key:data";

    byte[] initCommand();
}
