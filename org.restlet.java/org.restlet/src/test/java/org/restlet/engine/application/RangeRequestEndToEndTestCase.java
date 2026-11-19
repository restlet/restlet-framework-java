/**
 * Copyright 2005-2026 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/**
 * End-to-end tests of the handling of the "Range" request header (RFC 9110 section 14) by a real
 * server. The raw header is sent with the JDK client, as the Restlet client can only write valid
 * ranges.
 */
class RangeRequestEndToEndTestCase {

    private static final String BODY = "0123456789";

    private Component component;

    private HttpClient client;

    private URI uri;

    @BeforeEach
    void setUpEach() throws Exception {
        component = new Component();
        Server server = component.getServers().add(Protocol.HTTP, 0);
        component
                .getDefaultHost()
                .attach(
                        new Application() {
                            @Override
                            public Restlet createInboundRoot() {
                                return new Restlet() {
                                    @Override
                                    public void handle(Request request, Response response) {
                                        response.setEntity(
                                                new StringRepresentation(
                                                        BODY, MediaType.TEXT_PLAIN));
                                    }
                                };
                            }
                        });
        component.start();
        uri = URI.create("http://localhost:" + server.getActualPort() + "/");
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDownEach() throws Exception {
        component.stop();
        Engine.clearThreadLocalVariables();
    }

    private HttpResponse<String> get(String range) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET();
        if (range != null) {
            builder.header("Range", range);
        }
        return client.send(builder.build(), BodyHandlers.ofString());
    }

    private static String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElse(null);
    }

    private void assertPartial(String range, String body, String contentRange) throws Exception {
        HttpResponse<String> response = get(range);
        assertEquals(206, response.statusCode(), range);
        assertEquals(body, response.body(), range);
        assertEquals(contentRange, header(response, "Content-Range"), range);
    }

    @Test
    void noRange_returnsTheWholeEntityAndAdvertisesRanges() throws Exception {
        HttpResponse<String> response = get(null);
        assertEquals(200, response.statusCode());
        assertEquals(BODY, response.body());
        assertEquals("bytes", header(response, "Accept-Ranges"));
    }

    @Test
    void satisfiableRanges_returnPartialContent() throws Exception {
        assertPartial("bytes=2-4", "234", "bytes 2-4/10");
        assertPartial("bytes=7-", "789", "bytes 7-9/10");
        assertPartial("bytes=9-", "9", "bytes 9-9/10");
        assertPartial("bytes=-4", "6789", "bytes 6-9/10");
        assertPartial("bytes=5-100", "56789", "bytes 5-9/10");
        assertPartial("bytes=8-12", "89", "bytes 8-9/10");
    }

    /** RFC 9110 section 14.1.2: a suffix longer than the representation selects all of it. */
    @Test
    void suffixLongerThanEntity_returnsTheWholeEntity() throws Exception {
        assertPartial("bytes=-500", BODY, "bytes 0-9/10");
    }

    @Test
    void unitName_isCaseInsensitive() throws Exception {
        assertPartial("Bytes=0-1", "01", "bytes 0-1/10");
    }

    /** RFC 9110 section 14.2: a Range header that is invalid or has an unknown unit is ignored. */
    @ParameterizedTest
    @ValueSource(
            strings = {
                "bytes=a-b",
                "bytes=",
                "bytes=-",
                "bytes=5-3",
                "bytes=0-1,abc",
                "bytes=99999999999999999999-",
                "items=0-1",
                "0-1"
            })
    void invalidOrUnknownRange_isIgnored(String range) throws Exception {
        HttpResponse<String> response = get(range);
        assertEquals(200, response.statusCode(), range);
        assertEquals(BODY, response.body(), range);
        assertEquals(null, header(response, "Content-Range"), range);
    }

    /**
     * RFC 9110 sections 14.2 and 14.4: a satisfiable-looking but unsatisfiable range gets a 416
     * with the complete length of the representation.
     */
    @ParameterizedTest
    @ValueSource(strings = {"bytes=10-", "bytes=10-20", "bytes=1000-", "bytes=-0"})
    void unsatisfiableRange_returns416WithTheCompleteLength(String range) throws Exception {
        HttpResponse<String> response = get(range);
        assertEquals(416, response.statusCode(), range);
        assertEquals("bytes */10", header(response, "Content-Range"), range);
    }
}
