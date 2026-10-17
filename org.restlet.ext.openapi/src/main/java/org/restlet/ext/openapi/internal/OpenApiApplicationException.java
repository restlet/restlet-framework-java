/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.internal;

import java.io.Serial;
import org.restlet.resource.Status;

@Status(500)
public class OpenApiApplicationException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public OpenApiApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
