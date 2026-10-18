/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.websocket;

/**
 * Handles an accepted WebSocket session with straight-line blocking code. It runs on its own
 * virtual thread; when it returns, the session is closed normally, and when it throws, the session
 * is closed with {@link WebSocketSession#INTERNAL_ERROR}.
 */
@FunctionalInterface
public interface WebSocketHandler {

    /**
     * Handles the session until it is done.
     *
     * @param session The open session.
     * @throws Exception If the handling fails.
     */
    void handle(WebSocketSession session) throws Exception;
}
