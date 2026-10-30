/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import org.restlet.Request;
import org.restlet.Response;

/**
 * Handles connection upgrades for {@link GraphqlServerAdapter}. The implementation depends on the
 * optional WebSocket extension: the adapter only refers to it through this interface, so that the
 * extension is loaded only when {@link GraphqlTransport#WEBSOCKET} is enabled.
 */
interface GraphqlUpgradeSupport {

    /**
     * Handles a request if it asks for a connection upgrade.
     *
     * @param request The request.
     * @param response The response.
     * @return True if the request asked for an upgrade and was handled.
     */
    boolean handle(Request request, Response response);

    /** Closes the open connections. */
    void stop();
}
