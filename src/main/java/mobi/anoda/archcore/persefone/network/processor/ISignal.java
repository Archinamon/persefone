package mobi.anoda.archcore.persefone.network.processor;

import android.os.Parcelable;
import mobi.anoda.archcore.persefone.signals.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignal extends Parcelable  {

    static String BASE     = "mobi.anoda.archcore.signal";
    static String KEY_DATA = ".key:data";

    byte[] initCommand();
}
