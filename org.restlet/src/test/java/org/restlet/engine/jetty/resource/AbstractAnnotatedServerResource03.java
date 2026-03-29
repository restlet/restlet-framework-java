/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.jetty.resource;

import org.restlet.resource.ServerResource;

/**
 * Abstract {@link ServerResource} that implements several annotated interfaces.
 *
 * @author Thierry Boileau
 */
public abstract class AbstractAnnotatedServerResource03 extends ServerResource
        implements AnnotatedInterface03 {

    public String accept() {
        return "accept";
    }

    public String asText() {
        return "asText";
    }
}
