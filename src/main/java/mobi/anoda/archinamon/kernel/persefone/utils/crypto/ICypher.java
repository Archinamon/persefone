package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

import android.support.annotation.NonNull;

/**
 * Created by matsukov-ea on 17.09.2014.
 */
public interface ICypher {

    public static final class HexString {

        // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
        private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

        public static String parse(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }

    final Object STATIC_LOCK = new Object();

    String encrypt(@NonNull final String input) throws Exception;

    String decrypt(@NonNull final String cypher) throws Exception;
}
