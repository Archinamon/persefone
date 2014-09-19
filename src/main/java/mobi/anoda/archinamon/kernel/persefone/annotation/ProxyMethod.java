package mobi.anoda.archinamon.kernel.persefone.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Archinamon on 5/8/14.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface ProxyMethod {

    String value() default "NPE";
}
