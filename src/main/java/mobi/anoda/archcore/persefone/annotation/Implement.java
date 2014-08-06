package mobi.anoda.archcore.persefone.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author: Archinamon
 */
@Target({ElementType.METHOD,
         ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Implement {
}
