/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;

class WebSocketUpgradeTestCase {

    private static Request upgradeRequest(String... subprotocols) {
        Request request = new Request(Method.GET, "http://localhost/ws");
        request.getHeaders().add("Upgrade", "WebSocket");
        request.getHeaders().add("Connection", "keep-alive, Upgrade");
        if (subprotocols.length > 0) {
            request.getHeaders().add("Sec-WebSocket-Protocol", String.join(", ", subprotocols));
        }
        return request;
    }

    @Test
    void detectsUpgradeRequests() {
        assertTrue(WebSocketUpgrade.isRequested(upgradeRequest()));
        assertFalse(WebSocketUpgrade.isRequested(new Request(Method.GET, "http://localhost/")));

        Request post = upgradeRequest();
        post.setMethod(Method.POST);
        assertFalse(WebSocketUpgrade.isRequested(post));
    }

    @Test
    void parsesOfferedSubprotocols() {
        assertEquals(
                List.of("a", "b"),
                WebSocketUpgrade.getRequestedSubprotocols(upgradeRequest("a", " b")));
        assertEquals(List.of(), WebSocketUpgrade.getRequestedSubprotocols(upgradeRequest()));
    }

    @Test
    void acceptRecordsTheUpgradeOnTheResponse() {
        Response response = new Response(upgradeRequest("chat"));
        WebSocketHandler handler = session -> {};

        WebSocketUpgrade.accept(response, handler, "chat");

        assertEquals(Status.INFO_SWITCHING_PROTOCOL, response.getStatus());
        WebSocketUpgrade upgrade = WebSocketUpgrade.get(response);
        assertSame(handler, upgrade.getHandler());
        assertEquals("chat", upgrade.getSubprotocol());
    }

    @Test
    void acceptRejectsInvalidUpgrades() {
        Response plain = new Response(new Request(Method.GET, "http://localhost/"));
        WebSocketHandler handler = session -> {};
        assertThrows(
                IllegalStateException.class, () -> WebSocketUpgrade.accept(plain, handler, null));
        Response chat = new Response(upgradeRequest("chat"));
        assertThrows(
                IllegalArgumentException.class,
                () -> WebSocketUpgrade.accept(chat, handler, "other"));
        assertNull(WebSocketUpgrade.get(new Response(upgradeRequest())));
    }

    @Test
    void resourceRejectsWhenNoHandler() {
        WebSocketUpgrader refusing =
                new WebSocketUpgrader() {
                    @Override
                    protected WebSocketHandler accept(Request request, Response response) {
                        return null;
                    }
                };
        Response response = new Response(upgradeRequest());
        refusing.handle(response.getRequest(), response);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
        assertNull(WebSocketUpgrade.get(response));
    }

    @Test
    void messagesConvertBetweenTextAndBinary() {
        WebSocketMessage text = WebSocketMessage.text("é");
        assertTrue(text.isText());
        assertArrayEquals("é".getBytes(StandardCharsets.UTF_8), text.getData());

        byte[] bytes = "é".getBytes(StandardCharsets.UTF_8);
        WebSocketMessage binary = WebSocketMessage.binary(bytes);
        bytes[0] = 0;
        assertTrue(binary.isBinary());
        assertEquals("é", binary.getText());
        assertEquals(WebSocketMessage.binary(binary.getData()), binary);
        assertEquals("binary[2 bytes]", binary.toString());
    }
}
