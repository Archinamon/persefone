package mobi.anoda.archinamon.kernel.persefone.stateless.delegates;

/**
 * Represents a function that accepts no input and produces a result
 *
 * @param <R> Result type
 */
public interface Func<R> {

    /**
     * Applies this function
     *
     * @return Result
     */
    R call();
}