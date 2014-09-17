package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

final class RSA implements ICypher {

    public static final String TAG = RSA.class.getSimpleName();
    private static ICypher    INSTANCE;
    private        PrivateKey mPrivateKey;
    private        byte[]     mEncryptedBytes;

    public static ICypher getInstance() {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new RSA();
            }
        }

        return INSTANCE;
    }

    private RSA() {}

    @Implement
    public final String encrypt(@NonNull final String input) {
        try {
            KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            pairGenerator.initialize(1024);
            KeyPair keyPair = pairGenerator.genKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            mPrivateKey = keyPair.getPrivate();

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            mEncryptedBytes = cipher.doFinal(input.getBytes());
            return ICypher.HexString.parse(mEncryptedBytes);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LogHelper.println_error(TAG, e);
            return null;
        }
    }

    @Implement
    public final String decrypt(@NonNull final String cypher) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, mPrivateKey);
            byte[] decryptedBytes = cipher.doFinal(mEncryptedBytes);
            return ICypher.HexString.parse(decryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            LogHelper.println_error(TAG, e);
            return null;
        }
    }
}