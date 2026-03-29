/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.jetty.resource;

import org.restlet.resource.Post;

/**
 * Annotated interface that declares a single "Post" method.
 *
 * @author Thierry Boileau
 */
public interface AnnotatedInterface03_02 {

    @Post
    String accept();
}
