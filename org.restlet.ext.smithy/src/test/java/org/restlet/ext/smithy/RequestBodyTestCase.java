/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import software.amazon.smithy.java.io.datastream.DataStream;

class RequestBodyTestCase {
    private static byte[] write(Representation body) throws IOException {
        var out = new ByteArrayOutputStream();
        body.write(out);
        return out.toByteArray();
    }

    /**
     * The SDK releases a pooled body when its call ends, and may then reuse its array for another
     * call, while the client is still writing the body after an early response.
     */
    @Test
    void pooledBodyOutlivesItsCall() throws IOException {
        byte[] array = "--payload--".getBytes(StandardCharsets.US_ASCII);
        var pooled =
                DataStream.ofLeasedBytes(
                        array,
                        2,
                        7,
                        "application/octet-stream",
                        recycled -> Arrays.fill(recycled, (byte) 'x'));
        Representation body = RequestBody.of(pooled, MediaType.APPLICATION_OCTET_STREAM);
        pooled.release();
        assertEquals(7, body.getSize());
        assertFalse(body.isTransient());
        // Replayable, without a copy.
        assertArrayEquals("payload".getBytes(StandardCharsets.US_ASCII), write(body));
        assertArrayEquals("payload".getBytes(StandardCharsets.US_ASCII), write(body));
    }

    @Test
    void buffersAreWrittenWithoutTheirArrays() throws IOException {
        var direct = ByteBuffer.allocateDirect(40 * 1024);
        for (int i = 0; i < direct.capacity(); i++) {
            direct.put((byte) i);
        }
        direct.flip();
        var expected = new byte[direct.remaining() + 3];
        direct.duplicate().get(expected, 0, direct.remaining());
        System.arraycopy(new byte[] {1, 2, 3}, 0, expected, direct.remaining(), 3);
        Representation body =
                RequestBody.of(
                        DataStream.ofByteBuffers(
                                List.of(
                                        direct,
                                        ByteBuffer.wrap(new byte[] {1, 2, 3}).asReadOnlyBuffer()),
                                expected.length,
                                null),
                        null);
        assertEquals(expected.length, body.getSize());
        assertArrayEquals(expected, write(body));
    }

    @Test
    void streamedBodyIsReadOnce() throws IOException {
        byte[] payload = "streamed".getBytes(StandardCharsets.US_ASCII);
        Representation body =
                RequestBody.of(DataStream.ofInputStream(new ByteArrayInputStream(payload)), null);
        assertEquals(Representation.UNKNOWN_SIZE, body.getSize());
        assertTrue(body.isTransient());
        assertArrayEquals(payload, write(body));
        Representation sized =
                RequestBody.of(
                        DataStream.ofInputStream(
                                new ByteArrayInputStream(payload), null, payload.length),
                        null);
        assertEquals(payload.length, sized.getSize());
    }

    @Test
    void emptyBodyHasNoRepresentation() {
        assertNull(RequestBody.of(null, null));
        assertNull(RequestBody.of(DataStream.ofEmpty(), null));
    }
}
