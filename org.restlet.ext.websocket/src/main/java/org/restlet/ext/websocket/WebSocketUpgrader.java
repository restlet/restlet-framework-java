/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import java.util.List;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Restlet accepting WebSocket upgrades. Attach it to a router like any Restlet: filters and
 * authenticators in front of it apply to the upgrade request. Requests that do not ask for an
 * upgrade receive a 400 (Bad Request) response.
 */
public abstract class WebSocketUpgrader extends Restlet {

    /** Constructor. */
    protected WebSocketUpgrader() {
        super();
    }

    /**
     * Constructor.
     *
     * @param context The context.
     */
    protected WebSocketUpgrader(Context context) {
        super(context);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        if (!WebSocketUpgrade.isRequested(request)) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            response.setEntity("A WebSocket upgrade is required", MediaType.TEXT_PLAIN);
            return;
        }

        WebSocketHandler handler = accept(request, response);
        if (handler == null) {
            if (response.getStatus() == null || response.getStatus().isSuccess()) {
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
            return;
        }
        WebSocketUpgrade.accept(
                response,
                handler,
                selectSubprotocol(WebSocketUpgrade.getRequestedSubprotocols(request)));
    }

    /**
     * Accepts or rejects an upgrade request.
     *
     * @param request The upgrade request.
     * @param response The response, whose status may be set when rejecting.
     * @return The session handler, or null to reject the upgrade (403 by default).
     */
    protected abstract WebSocketHandler accept(Request request, Response response);

    /**
     * Selects a subprotocol among those offered by the client. By default, none is selected.
     *
     * @param requested The offered subprotocols, in preference order.
     * @return The selected subprotocol, or null.
     */
    protected String selectSubprotocol(List<String> requested) {
        return null;
    }
}
