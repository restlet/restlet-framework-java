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
                            router.getRoutes().get(0),
                            router.getNext(request, new Response(request)));
                    assertEquals("/review.Server/Echo", request.getOriginalRef().getPath());
                } finally {
                    channel.finishAndReleaseAll();
                }
            }
        }
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
