/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/**
 * WebSocket support (experimental): {@link org.restlet.ext.websocket.WebSocketResource} accepts
 * upgrades on the Netty HTTP server connector of the core, {@link
 * org.restlet.ext.websocket.WebSocketClient} opens client sessions, and both use the blocking
 * {@link org.restlet.ext.websocket.WebSocketSession}.
 *
 * <p>Server connector parameters read by this extension: "websocket.maxSessions" (maximum number of
 * open sessions, extra upgrades receive a 503 response; 0, the default, for no limit) and
 * "websocket.maxMessageSize" (maximum size in bytes of a received message, 65536 by default; larger
 * messages close the session with status 1009).
 */
package org.restlet.ext.websocket;
