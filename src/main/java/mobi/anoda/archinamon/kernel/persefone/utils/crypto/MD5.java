package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

final class MD5 extends HashCypher {

    public static final String TAG = MD5.class.getSimpleName();
    private static ICypher INSTANCE;

    public static ICypher getInstance() {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new MD5();
            }
        }

        return INSTANCE;
    }

    private MD5() {}

    @Implement
    public final String encrypt(@NonNull final String input) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(input.getBytes());
            byte bytes[] = digest.digest();

            return ICypher.HexString.parse(bytes);
        } catch (NoSuchAlgorithmException e) {
            LogHelper.println_error(TAG, e);
        }

        return "";
    }
}