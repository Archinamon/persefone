package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import java.security.MessageDigest;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

final class SHA256 extends HashCypher {

    public static final String TAG = SHA256.class.getSimpleName();
    private static ICypher INSTANCE;

    public static ICypher getInstance() {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new SHA256();
            }
        }

        return INSTANCE;
    }

    private SHA256() {
    }

    @Implement
    public final String encrypt(@NonNull final String toHash) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toHash.getBytes("UTF-8"));

            // This is ~55x faster than looping and String.formating()
            return ICypher.HexString.parse(hash);
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}