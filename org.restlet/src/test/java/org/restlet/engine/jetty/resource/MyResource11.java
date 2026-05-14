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
 * Resource that precises the media type of one of its inherited annotated methods.
 *
 * @author Thierry Boileau
 */
public class MyResource11 extends AbstractAnnotatedServerResource03 {

    @Get("txt")
    @Override
    public String asText() {
        return "asText-txt";
    }
}
