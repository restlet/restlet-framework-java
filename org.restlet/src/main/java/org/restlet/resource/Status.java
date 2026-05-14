/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for {@link Throwable} that map to HTTP error statuses. Its semantics are equivalent to
 * an HTTP status line plus a related HTTP entity for errors.<br>
 * <br>
 * Example:
 *
 * <pre>
 * &#064;Get
 * public MyBean represent() throws MyServerError, MyNotFoundError;
 *
 * &#064;Status(500)
 * public class MyServerError implements Throwable{
 *    ...
 * }
 *
 * &#064;Status(404, serialize = false)
 * public class MyNotFoundError extends RuntimeException{
 *    ...
 * }
 *
 * &#064;Status(value = 400)
 * public class MyBadParameterError extends RuntimeException{
 *    public String getParameterName() {
 *        ...
 *    };
 *    ...
 * }
 * </pre>
 *
 * @author Jerome Louvel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Status {

    /**
     * Specifies the HTTP status code associated with the annotated {@link Throwable}. Default is
     * 500.
     *
     * @return The result HTTP status code.
     */
    int value() default 500;

    /**
     * Indicates if the annotated {@link Throwable} should be serialized in the HTTP response
     * entity.
     *
     * @return True if {@link Throwable} should be serialized in the HTTP response entity.
     */
    boolean serialize() default true;
}
