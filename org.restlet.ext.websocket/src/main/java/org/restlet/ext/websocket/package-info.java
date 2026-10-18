/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/**
 * WebSocket support (experimental): {@link org.restlet.ext.websocket.WebSocketUpgrader} accepts
 * upgrades on the Netty HTTP server connector of the core, {@link
 * org.restlet.ext.websocket.WebSocketClient} opens client sessions, and both use the blocking
 * {@link org.restlet.ext.websocket.WebSocketSession}.
 *
 * <p>Server connector parameters read by this extension:
 *
 * <ul>
 *   <li>"websocket.maxSessions": maximum number of open sessions, extra upgrades receive a 503
 *       response; 0, the default, for no limit.
 *   <li>"websocket.maxMessageSize": maximum size in bytes of a received message, after
 *       decompression, 65536 by default; larger messages close the session with status 1009.
 *   <li>"websocket.maxFrameSize": maximum size in bytes of a received frame, the maximum message
 *       size by default (and at most); larger frames close the session with status 1009.
 *   <li>"websocket.compression": whether "permessage-deflate" compression (RFC 7692) is accepted
 *       when the client offers it, true by default.
 * </ul>
 */
package org.restlet.ext.websocket;
