package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * Created by matsukov-ea on 17.09.2014.
 */
abstract class HashCypher implements ICypher {

    @Implement
    public final String decrypt(@NonNull final String fromHash) {
        return null;
    }
}
