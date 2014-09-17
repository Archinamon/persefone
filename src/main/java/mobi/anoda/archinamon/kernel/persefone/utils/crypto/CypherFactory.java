package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

/**
 * Created by Archinamon on 8/2/14.
 */
@SuppressWarnings("FinalStaticMethod")
public final class CypherFactory {

    public static final String TAG = CypherFactory.class.getSimpleName();

    public ICypher obtainFor(Algorithm algorithm) {
        switch (algorithm) {
            case MD5:
                return MD5.getInstance();
            case SHA1:
                return SHA1.getInstance();
            case SHA256:
                return SHA256.getInstance();
            case SHA384:
                return SHA384.getInstance();
            case SHA512:
                return SHA512.getInstance();
            case RSA:
                return RSA.getInstance();
            case AES256:
                return AES256.getInstance();
            default: throw new UnsupportedAlgorithmException("Specify only supported algorithm!");
        }
    }
}
