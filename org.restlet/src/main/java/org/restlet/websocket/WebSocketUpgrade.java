/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Accepts a WebSocket upgrade on a Restlet response. The upgrade request goes through the normal
 * routing, filters and authenticators; the Restlet handling it calls {@link #accept(Response,
 * WebSocketHandler, String)}, and the HTTP server connector then performs the handshake and runs
 * the handler on its own virtual thread.
 *
 * <p>Only HTTP/1.1 upgrades are supported (not WebSocket over HTTP/2).
 */
public final class WebSocketUpgrade {

    /** Name of the response attribute holding the accepted upgrade. */
    public static final String ATTRIBUTE = "org.restlet.websocket.upgrade";

    private final WebSocketHandler handler;

    private final String subprotocol;

    private WebSocketUpgrade(WebSocketHandler handler, String subprotocol) {
        this.handler = handler;
        this.subprotocol = subprotocol;
    }

    /**
     * Indicates whether a request asks for a WebSocket upgrade: a GET with "Upgrade: websocket" and
     * "Connection: upgrade".
     *
     * @param request The request.
     * @return True if a WebSocket upgrade is requested.
     */
    public static boolean isRequested(Request request) {
        return Method.GET.equals(request.getMethod())
                && hasToken(request, "Upgrade", "websocket")
                && hasToken(request, "Connection", "upgrade");
    }

    /**
     * Returns the subprotocols offered by the client in "Sec-WebSocket-Protocol", in preference
     * order.
     *
     * @param request The request.
     * @return The offered subprotocols, possibly empty.
     */
    public static List<String> getRequestedSubprotocols(Request request) {
        List<String> result = new ArrayList<>();
        for (Header header : request.getHeaders()) {
            if ("Sec-WebSocket-Protocol".equalsIgnoreCase(header.getName())
                    && header.getValue() != null) {
                for (String token : header.getValue().split(",")) {
                    String value = token.trim();
                    if (!value.isEmpty()) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Accepts the upgrade: sets the "101 Switching Protocols" status and records the handler. The
     * server connector completes the handshake after the response leaves the Restlet chain, unless
     * the status was changed in between.
     *
     * @param response The response to the upgrade request.
     * @param handler The session handler.
     * @param subprotocol The selected subprotocol, one of those offered by the client, or null.
     */
    public static void accept(Response response, WebSocketHandler handler, String subprotocol) {
        Objects.requireNonNull(handler, "handler");
        Request request = response.getRequest();
        if (request == null || !isRequested(request)) {
            throw new IllegalStateException("The request does not ask for a WebSocket upgrade");
        }
        if (subprotocol != null && !getRequestedSubprotocols(request).contains(subprotocol)) {
            throw new IllegalArgumentException(
                    "The subprotocol \"" + subprotocol + "\" was not offered by the client");
        }
        response.getAttributes().put(ATTRIBUTE, new WebSocketUpgrade(handler, subprotocol));
        response.setStatus(Status.INFO_SWITCHING_PROTOCOL);
        response.setEntity(null);
    }

    /**
     * Returns the upgrade accepted on a response.
     *
     * @param response The response.
     * @return The accepted upgrade, or null.
     */
    public static WebSocketUpgrade get(Response response) {
        return (response.getAttributes().get(ATTRIBUTE) instanceof WebSocketUpgrade upgrade)
                ? upgrade
                : null;
    }

    /**
     * Returns the session handler.
     *
     * @return The session handler.
     */
    public WebSocketHandler getHandler() {
        return handler;
    }

    /**
     * Returns the selected subprotocol.
     *
     * @return The selected subprotocol, or null.
     */
    public String getSubprotocol() {
        return subprotocol;
    }

    private static boolean hasToken(Request request, String name, String token) {
        for (Header header : request.getHeaders()) {
            if (name.equalsIgnoreCase(header.getName()) && header.getValue() != null) {
                for (String value : header.getValue().split(",")) {
                    if (token.equals(value.trim().toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
