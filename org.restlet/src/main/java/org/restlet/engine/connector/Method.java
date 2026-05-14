/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Patch;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

/**
 * Meta annotation to declare method annotations.
 *
 * @see Get
 * @see Post
 * @see Put
 * @see Delete
 * @see Options
 * @see Patch
 * @author Jerome Louvel
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Method {

    /**
     * Method name identified by the underlying annotation.
     *
     * @return the method name identified by the underlying annotation.
     */
    String value();
}
