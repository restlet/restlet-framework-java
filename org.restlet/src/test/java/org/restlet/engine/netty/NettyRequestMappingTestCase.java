/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

class NettyRequestMappingTestCase {
    @Test
    void serviceRoutesMatchOriginAndSameHostAbsoluteTargetsForEveryHttpVersion() {
        for (StreamKind kind : StreamKind.values()) {
            for (String target :
                    new String[] {
                        "/review.Server/Echo?mode=one",
                        "review.Server/Echo?mode=one",
                        "http://localhost:8080/review.Server/Echo?mode=one"
                    }) {
                EmbeddedChannel channel = new EmbeddedChannel();
                try {
                    var head =
                            new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, target);
                    head.headers().set("host", "localhost:8080");
                    var call =
                            new NettyServerCall(
                                    null,
                                    channel,
                                    kind,
                                    head,
                                    new ChannelInputStream(channel, 1000),
                                    false);
                    var request = call.toRequest(new Context());
                    assertEquals("/review.Server/Echo", request.getResourceRef().getPath());
                    assertEquals("mode=one", request.getResourceRef().getQuery());
                    assertEquals(kind.version().substring(5), request.getProtocol().getVersion());
                    Router router = new Router();
                    Restlet service = new Restlet() {};
                    router.attach("/review.Server/", service, Template.MODE_STARTS_WITH);
                    assertSame(
                            router.getRoutes().getFirst(),
                            router.getNext(request, new Response(request)));
                    assertEquals("/review.Server/Echo", request.getOriginalRef().getPath());
                } finally {
                    channel.finishAndReleaseAll();
                }
            }
        }
    }

    private static org.restlet.Request map(
            StreamKind kind, HttpVersion version, String target, String host) {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            var head = new DefaultHttpRequest(version, HttpMethod.GET, target);
            head.headers().set("host", host);
            return new NettyServerCall(
                            null, channel, kind, head, new ChannelInputStream(channel, 1000), false)
                    .toRequest(new Context());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void absoluteTargetsWithAnEquivalentAuthorityMapToTheirPath() {
        for (String target :
                new String[] {
                    "http://example.com:80/rest?q=1",
                    "http://EXAMPLE.com/rest?q=1",
                    "HTTP://example.com/rest?q=1"
                }) {
            var request = map(StreamKind.HTTP2, HttpVersion.HTTP_1_1, target, "example.com:80");
            assertEquals("http://example.com/rest?q=1", request.getResourceRef().toString());
            assertEquals("/rest", request.getResourceRef().getRemainingPart(false, false));
        }
    }

    @Test
    void absoluteTargetsForAnotherAuthorityAreKept() {
        var request =
                map(StreamKind.HTTP1, HttpVersion.HTTP_1_1, "http://other.test:8080/rest", "a");
        assertEquals("http://other.test:8080/rest", request.getResourceRef().toString());
        assertEquals("http://a", request.getResourceRef().getBaseRef().toString());
    }

    @Test
    void http1ReportsTheVersionOfItsRequestLine() {
        assertEquals(
                "1.0",
                map(StreamKind.HTTP1, HttpVersion.HTTP_1_0, "/", "a").getProtocol().getVersion());
        assertEquals(
                "2.0",
                map(StreamKind.HTTP2, HttpVersion.HTTP_1_1, "/", "a").getProtocol().getVersion());
    }

    @Test
    void emptyTargetMapsToRoot() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            var head = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
            head.headers().set("host", "localhost");
            var call =
                    new NettyServerCall(
                            null,
                            channel,
                            StreamKind.HTTP1,
                            head,
                            new ChannelInputStream(channel, 1000),
                            false);
            var request = call.toRequest(new Context());
            assertEquals("/", request.getResourceRef().getPath());
            assertEquals("/", request.getResourceRef().getRemainingPart(false, false));
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
