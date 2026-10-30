/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionResult;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * A subscription transport of {@link GraphqlClientAdapter} other than SSE. The implementation
 * depends on the optional WebSocket extension: the adapter only refers to it through this
 * interface, so that the extension is loaded only when {@link GraphqlTransport#WEBSOCKET} is
 * selected.
 */
interface GraphqlSubscriptionTransport {

    /**
     * Subscribes to an operation.
     *
     * @param document The document.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return A cold publisher of the results.
     */
    Publisher<ExecutionResult> subscribe(
            String document, String operationName, Map<String, Object> variables);

    /** Fails the open subscriptions and closes the connection. */
    void stop();
}
