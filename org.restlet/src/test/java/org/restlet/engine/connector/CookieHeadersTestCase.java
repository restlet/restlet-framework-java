/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.SameSite;
import org.restlet.engine.Engine;

/** Tests the cookie headers on the wire, as written and read by the Netty server connector. */
class CookieHeadersTestCase {

    private Server server;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
    }

    /** Sends a raw request and returns the raw response head and body. */
    private String exchange(String requestHeaders) throws Exception {
        try (Socket socket = new Socket("localhost", server.getActualPort())) {
            OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET / HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
                                    + requestHeaders
                                    + "\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();

            InputStream in = socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return buffer.toString(StandardCharsets.ISO_8859_1);
        }
    }

    private static List<String> headerValues(String response, String name) {
        List<String> result = new ArrayList<>();
        for (String line : response.split("\r\n")) {
            if (line.regionMatches(true, 0, name + ":", 0, name.length() + 1)) {
                result.add(line.substring(name.length() + 1).trim());
            }
        }
        return result;
    }

    private void startEchoingServer() throws Exception {
        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                StringBuilder sb = new StringBuilder();
                                for (Cookie cookie : request.getCookies()) {
                                    sb.append(cookie.getName())
                                            .append('=')
                                            .append(cookie.getValue())
                                            .append('|');
                                }
                                response.setEntity(sb.toString(), MediaType.TEXT_PLAIN);

                                CookieSetting session = new CookieSetting("session", "abc");
                                session.setPath("/app");
                                session.setMaxAge(60);
                                session.setSecure(true);
                                session.setAccessRestricted(true);
                                session.setSameSite(SameSite.LAX);
                                response.getCookieSettings().add(session);
                                response.getCookieSettings().add(new CookieSetting("plain", "1"));
                            }
                        });
        server.start();
    }

    @Test
    void setCookie_isWrittenAsRfc6265() throws Exception {
        startEchoingServer();
        String response = exchange("");

        List<String> setCookies = headerValues(response, "Set-Cookie");
        assertEquals(2, setCookies.size(), response);
        assertTrue(setCookies.get(0).startsWith("session=abc; Path=/app; Expires="), response);
        assertTrue(
                setCookies.get(0).endsWith("; Max-Age=60; Secure; HttpOnly; SameSite=Lax"),
                response);
        assertEquals("plain=1", setCookies.get(1));
        assertFalse(response.contains("Set-Cookie2"), response);
        assertFalse(setCookies.get(0).contains("\""), response);
        assertFalse(setCookies.get(0).contains("Version"), response);
    }

    @Test
    void cookie_sentInOneHeader_isRead() throws Exception {
        startEchoingServer();
        assertTrue(exchange("Cookie: a=1; b=2\r\n").endsWith("a=1|b=2|"));
    }

    @Test
    void cookie_splitInSeveralHeaders_isRead() throws Exception {
        // HTTP/2 clients split the cookies, HTTP/1.1 allows it as well
        startEchoingServer();
        assertTrue(exchange("Cookie: a=1\r\nCookie: b=2\r\n").endsWith("a=1|b=2|"));
    }

    @Test
    void cookie_inLegacyFormat_isReadLeniently() throws Exception {
        startEchoingServer();
        assertTrue(
                exchange("Cookie: $Version=\"1\"; a=\"1\"; $Path=\"/x\"; b=\"2\"\r\n")
                        .endsWith("a=1|b=2|"));
    }
}
