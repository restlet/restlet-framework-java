/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import java.io.IOException;
import org.restlet.data.Status;

/**
 * Thrown by {@link GraphqlClientAdapter} when no GraphQL response could be obtained: connection
 * failures, timeouts, responses that are too large, and responses that are not GraphQL, such as an
 * HTML error page or an empty {@code 401}. Errors reported by the GraphQL server are returned in
 * the {@code ExecutionResult} instead.
 */
public class GraphqlTransportException extends IOException {

    private static final long serialVersionUID = 1L;

    private final transient Status status;

    private final String bodyExcerpt;

    /**
     * Constructor.
     *
     * @param message The message.
     * @param status The response status, or null if no response was received.
     * @param bodyExcerpt The beginning of the response body, or null.
     * @param cause The cause, or null.
     */
    public GraphqlTransportException(
            String message, Status status, String bodyExcerpt, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.bodyExcerpt = bodyExcerpt;
    }

    /**
     * Returns the response status.
     *
     * @return The response status, or null if no response was received.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the beginning of the response body, decoded as UTF-8 and bounded in size.
     *
     * @return The beginning of the response body, or null.
     */
    public String getBodyExcerpt() {
        return bodyExcerpt;
    }
}
