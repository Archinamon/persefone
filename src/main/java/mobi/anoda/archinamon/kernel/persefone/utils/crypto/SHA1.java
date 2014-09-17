package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

final class SHA1 extends HashCypher {

    public static final String TAG = SHA1.class.getSimpleName();
    private static ICypher INSTANCE;

    public static ICypher getInstance() {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new SHA1();
            }
        }

        return INSTANCE;
    }

    private SHA1() {}

    @Implement
    public String encrypt(@NonNull final String toHash) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = ICypher.HexString.parse(bytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LogHelper.println_error(TAG, e);
        }
        return hash;
    }
}