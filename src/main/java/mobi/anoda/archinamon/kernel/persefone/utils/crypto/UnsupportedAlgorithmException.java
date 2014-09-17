package mobi.anoda.archinamon.kernel.persefone.utils.crypto;

/**
 * Created by matsukov-ea on 17.09.2014.
 */
public class UnsupportedAlgorithmException extends RuntimeException {

    private static final long serialVersionUID = 8884846979050644673L;

    /**
     * Constructs a new {@code UnsupportedAlgorithmException} that includes the
     * current stack trace.
     */
    public UnsupportedAlgorithmException() {
    }

    /**
     * Constructs a new {@code UnsupportedAlgorithmException} with the current
     * stack trace and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public UnsupportedAlgorithmException(String detailMessage) {
        super(detailMessage);
    }
}
