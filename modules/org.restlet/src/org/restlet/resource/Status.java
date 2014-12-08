/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for {@link Throwable} that map to HTTP error statuses. Its
 * semantics is equivalent to an HTTP status line plus a related HTTP entity for
 * errors.<br>
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
     * Specifies the HTTP status code associated to the annotated
     * {@link Throwable}. Default is 500.
     * 
     * @return The result HTTP status code.
     */
    int value() default 500;

    /**
     * Indicates if the annotated {@link Throwable} should be serialized in the
     * HTTP response entity.
     * 
     * @return True if {@link Throwable} should be serialized in the HTTP
     *         response entity.
     */
    boolean serialize() default true;

}
