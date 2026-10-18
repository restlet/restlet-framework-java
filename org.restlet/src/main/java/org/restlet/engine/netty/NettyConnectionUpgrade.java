/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

/**
 * Takes over an HTTP/1.1 server connection after a "101 Switching Protocols" response, for example
 * to speak WebSocket. Register it as a response attribute named {@link #ATTRIBUTE} and set the
 * response status to 101: once the response leaves the Restlet chain, the Netty server connector
 * removes its HTTP handlers from the connection pipeline (keeping the HTTP codec) and calls {@link
 * #upgrade(NettyUpgradeContext)} instead of writing the response. The upgrade is then responsible
 * for the handshake response and for the rest of the connection.
 *
 * <p>Upgrades are only supported on HTTP/1.1 connections; on HTTP/2 and HTTP/3 the connector
 * answers 501 (Not Implemented).
 *
 * <p>This is an engine extension point, meant for Restlet extensions.
 */
@FunctionalInterface
public interface NettyConnectionUpgrade {

    /** Name of the response attribute holding the connection upgrade. */
    String ATTRIBUTE = "org.restlet.engine.netty.connectionUpgrade";

    /**
     * Takes over the connection. Called on the thread of the call, which must not be blocked for
     * the lifetime of the connection. If it throws, the connection is closed.
     *
     * @param context The upgrade context.
     * @throws Exception If the upgrade fails.
     */
    void upgrade(NettyUpgradeContext context) throws Exception;
}
