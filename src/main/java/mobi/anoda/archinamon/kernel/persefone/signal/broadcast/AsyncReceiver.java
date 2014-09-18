package mobi.anoda.archinamon.kernel.persefone.signal.broadcast;

import android.content.Intent;
import java.lang.Thread.UncaughtExceptionHandler;
import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

/**
* @author: Archinamon
* @project: Wallpapers2
*/
public interface AsyncReceiver {

    /**
     * Calling inside the {@link AbstractActivity} when new broadcastable intent caught
     *
     * @param incoming {@link Intent} with data sent with Broadcast message
     *
     * @return #true if message was successfully delivered and processed; #false otherwise
     */
    boolean onReceive(@NonNull final String action, Intent incoming);

    /**
     * Called if there was {@code UncaughtException} occured inside the pcall of {@code onReceive} method
     *
     * @param error throwed by {@link UncaughtExceptionHandler}
     */
    void onException(Throwable error);
}
