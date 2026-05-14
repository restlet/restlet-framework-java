/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.jetty.resource;

import org.restlet.resource.Get;

/**
 * Annotated interface that declares a single "Get" method.
 *
 * @author Thierry Boileau
 */
public interface AnnotatedInterface03_01 {

    @Get
    String asText();
}
