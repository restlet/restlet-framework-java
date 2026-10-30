/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

/**
 * Transports of GraphQL subscriptions, enabled with {@link
 * GraphqlServerAdapter#getSubscriptionTransports()}.
 */
public enum GraphqlTransport {

    /**
     * Server-Sent Events, following the {@code graphql-sse} protocol in distinct connections mode:
     * a request accepting {@code text/event-stream} gets one {@code next} event per result and a
     * final {@code complete} event.
     */
    SSE,

    /**
     * WebSocket, following the {@code graphql-transport-ws} protocol: the adapter accepts upgrades
     * offering this subprotocol, on HTTP/1.1 connections only. Requires the {@code
     * org.restlet.ext.websocket} module.
     */
    WEBSOCKET
}
