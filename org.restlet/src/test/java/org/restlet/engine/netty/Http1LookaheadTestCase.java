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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.restlet.engine.connector.HttpServerHelper;

class Http1LookaheadTestCase {

    @Test
    void acceptsExactlyOneMiBThenRejectsOverflowAndReleasesContent() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var full = new DefaultHttpContent(Unpooled.buffer(1024 * 1024).writeZero(1024 * 1024));
        var overflow = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
        try {
            channel.writeInbound(full);
            assertTrue(channel.isActive());
            assertEquals(1, full.refCnt());
            channel.writeInbound(overflow);
            assertFalse(channel.isActive());
            assertEquals(0, full.refCnt());
            assertEquals(0, overflow.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void acceptsExactly1024MessagesThenRejectsOverflowAndReleasesContent() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var content = new ArrayList<DefaultHttpContent>();
        try {
            for (int i = 0; i < 1024; i++) {
                var message = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
                content.add(message);
                channel.writeInbound(message);
            }
            assertTrue(channel.isActive());
            var overflow = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
            content.add(overflow);
            channel.writeInbound(overflow);
            assertFalse(channel.isActive());
            for (var message : content) {
                assertEquals(0, message.refCnt());
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void releasesQueuedContentWhenTheHandlerIsReplacedForAnUpgrade() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var content = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
        try {
            channel.writeInbound(content);
            channel.pipeline().remove(NettyServerHandler.class);
            assertTrue(channel.isActive());
            assertEquals(0, content.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @SuppressWarnings("unchecked")
    private static EmbeddedChannel waitingForResponse() throws Exception {
        var helper = new HttpServerHelper(null);
        var handler = new NettyServerHandler(helper, false, StreamKind.HTTP1);
        var channel = new EmbeddedChannel(handler);
        var call =
                new NettyServerCall(
                        helper,
                        channel,
                        StreamKind.HTTP1,
                        new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"),
                        new ChannelInputStream(channel, 0),
                        false);
        var field = NettyServerHandler.class.getDeclaredField("activeCall");
        field.setAccessible(true);
        ((AtomicReference<NettyServerCall>) field.get(handler)).set(call);
        return channel;
    }
}
