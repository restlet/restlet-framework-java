/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.ServerSentEvent;

class EventStreamTestCase {

    @Test
    void serializesUtf8FieldsAndMultilineValues() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EventStreamRepresentation representation =
                new EventStreamRepresentation(
                        sink -> {
                            sink.send(
                                    ServerSentEvent.builder()
                                            .id("42")
                                            .event("message")
                                            .data(" héllo\nworld ")
                                            .retry(250L)
                                            .comment("heartbeat")
                                            .build());
                        });

        representation.write(output);

        assertEquals(MediaType.TEXT_EVENT_STREAM, representation.getMediaType());
        assertEquals(StandardCharsets.UTF_8.name(), representation.getCharacterSet().getName());
        assertEquals(
                """
                id: 42
                event: message
                data:  héllo
                data: world\s
                retry: 250
                : heartbeat

                """,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void emitsConfiguredCommentHeartbeatsWhileTheWriterIsIdle() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EventStreamRepresentation representation =
                new EventStreamRepresentation(
                        sink -> {
                            try {
                                // Keeps the writer idle long enough for several heartbeats.
                                boolean released =
                                        new CountDownLatch(1).await(60, TimeUnit.MILLISECONDS);
                                assertFalse(released);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Interrupted while awaiting an event", e);
                            }
                            sink.send(ServerSentEvent.builder().data("ready").build());
                        },
                        Duration.ofMillis(10));

        representation.write(output);

        String content = output.toString(StandardCharsets.UTF_8);
        assertTrue(content.contains(":\n\n"), content);
        assertTrue(content.contains("data: ready\n\n"), content);
    }

    @Test
    void readsFieldsCommentsAndReconnectState() throws IOException {
        EventStreamReader reader =
                new EventStreamReader(
                        new StringReader(
                                """
                                \uFEFFid:cursor-1\r
                                event:update\r
                                data:first\r
                                data:second\r
                                retry:5\r
                                \r
                                :keepalive

                                data:

                                """));

        ServerSentEvent event = reader.readEvent();
        assertEquals("cursor-1", event.getId());
        assertEquals("update", event.getEvent());
        assertEquals("first\nsecond", event.getData());
        assertEquals(5L, event.getRetry());
        assertEquals(5L, reader.getRetryDelayMs());
        assertEquals("cursor-1", reader.getLastEventId());

        ServerSentEvent heartbeat = reader.readEvent();
        assertEquals("keepalive", heartbeat.getComment());
        assertNull(heartbeat.getData());

        assertEquals("", reader.readEvent().getData());
        assertNull(reader.readEvent());
        reader.close();
    }

    @Test
    void reconnectsAfterRetryAndPassesTheLastEventId() throws IOException {
        List<String> ids = new ArrayList<>();
        int[] attempts = {0};
        EventStreamReader reader =
                new EventStreamReader(
                        lastEventId -> {
                            ids.add(lastEventId);
                            return switch (attempts[0]++) {
                                case 0 -> stream("id:next\nretry:0\ndata:first\n\n");
                                case 1 -> stream("data:second\n\n");
                                default -> null;
                            };
                        });

        assertEquals("first", reader.readEvent().getData());
        assertEquals("second", reader.readEvent().getData());
        assertNull(reader.readEvent());
        assertNull(reader.readEvent());
        assertEquals(List.of("", "next", "next"), ids);
        reader.close();
    }

    @Test
    void validatesEventFields() {
        ServerSentEvent.Builder builder = ServerSentEvent.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.id("bad\nid"));
        assertThrows(IllegalArgumentException.class, () -> builder.event("bad\nevent"));
        assertThrows(IllegalArgumentException.class, () -> builder.retry(-1L));
    }

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
