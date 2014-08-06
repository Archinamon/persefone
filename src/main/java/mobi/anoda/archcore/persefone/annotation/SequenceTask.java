package mobi.anoda.archcore.persefone.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Archinamon on 5/28/14.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SequenceTask {

    public static enum Type {

        SYNC_UI,
        ASYNC
    }

    public static enum Order {

        PRE_COMPILE,
        POST_COMPILE,
        RUNTIME
    }

    Type value() default Type.SYNC_UI;

    Order exec_order() default Order.RUNTIME;
}
