package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;
import mobi.anoda.archinamon.kernel.persefone.utils.billing.Base64;

final class AES256 implements ICypher {

    public static final  String TAG                 = AES256.class.getSimpleName();
    private static final String CRYPTO_KEY          = "";
    private static       int    sPasswordIterations = 65536;
    private static       int    sKeySize            = 256;
    private static ICypher INSTANCE;
    private static String sSalt;
    private        byte[] mInitVectorBytes;

    public static ICypher getInstance() {
        if (INSTANCE == null) {
            synchronized (STATIC_LOCK) {
                INSTANCE = new AES256();
            }
        }

        return INSTANCE;
    }

    private AES256() {}

    @Implement
    public String encrypt(@NonNull final String plainText) throws Exception {
        //get salt
        sSalt = generateSalt();
        byte[] saltBytes = sSalt.getBytes("UTF-8");

        // Derive the key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(CRYPTO_KEY.toCharArray(), saltBytes, sPasswordIterations, sKeySize);

        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        //encrypt the message
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        mInitVectorBytes = params.getParameterSpec(IvParameterSpec.class)
                                 .getIV();
        byte[] encryptedTextBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.encode(encryptedTextBytes);
    }

    @Implement
    @SuppressWarnings("static-access")
    public String decrypt(@NonNull final String encryptedText) throws Exception {
        byte[] saltBytes = sSalt.getBytes("UTF-8");
        byte[] encryptedTextBytes = Base64.decode(encryptedText);

        // Derive the key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(CRYPTO_KEY.toCharArray(), saltBytes, sPasswordIterations, sKeySize);

        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        // Decrypt the message
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(mInitVectorBytes));

        byte[] decryptedTextBytes = null;
        try {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LogHelper.println_error(TAG, e);
        }

        return new String(decryptedTextBytes != null ? decryptedTextBytes : new byte[0]);
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return new String(bytes);
    }
}