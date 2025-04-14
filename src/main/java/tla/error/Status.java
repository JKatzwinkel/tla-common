package tla.error;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP status code a {@link TlaStatusCodeException} is about.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Status {
    /**
     * Status code as numeric value.
     * @return numeric status code
     */
    int value();
}
