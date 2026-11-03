/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.io.datastream.PooledDataStream;

/**
 * Request body of an SDK call, written by the Restlet client.
 *
 * <p>The HTTP/2 client may still be writing it after an early response has ended the SDK call. An
 * in-memory body is therefore captured as buffers while the call is in progress: a pooled one is
 * abandoned first, so that the SDK does not recycle its array when the call ends. A streamed body
 * is read as the client writes it, and closed when the client releases it unless replayable.
 */
final class RequestBody {
    private static final int CHUNK = 16 * 1024;

    private RequestBody() {}

    /**
     * Returns the representation of an SDK request body, or null when it is empty.
     *
     * @param body The SDK request body, possibly null.
     * @param mediaType The media type.
     * @return The representation, or null.
     */
    static Representation of(DataStream body, MediaType mediaType) {
        if (body == null) {
            return null;
        }
        if (body.contentLength() == 0) {
            closeUnlessReplayable(body);
            return null;
        }
        boolean pooled = body instanceof PooledDataStream;
        if (pooled) {
            ((PooledDataStream) body).abandon();
        }
        if (pooled || body.hasByteBuffer()) {
            List<ByteBuffer> buffers = new ArrayList<>();
            // Views of the SDK's arrays, not copies.
            body.forEachBuffer(buffer -> buffers.add(buffer.duplicate()));
            long size = buffers.stream().mapToLong(ByteBuffer::remaining).sum();
            // Replayable.
            return new OutputRepresentation(mediaType, size) {
                @Override
                public void write(OutputStream out) throws IOException {
                    RequestBody.write(buffers, out);
                }
            };
        }
        Representation result =
                new OutputRepresentation(
                        mediaType,
                        body.hasKnownLength()
                                ? body.contentLength()
                                : Representation.UNKNOWN_SIZE) {
                    @Override
                    public void write(OutputStream out) throws IOException {
                        body.writeTo(out);
                    }

                    @Override
                    public void release() {
                        super.release();
                        closeUnlessReplayable(body);
                    }
                };
        result.setTransient(true);
        return result;
    }

    /**
     * Closes a body that can't be read again. Writing a stream does not close it, and closing it
     * unblocks a writer reading it. A replayable body, such as a file, stays open for a retry.
     */
    private static void closeUnlessReplayable(DataStream body) {
        if (!body.isReplayable()) {
            body.close();
        }
    }

    private static void write(List<ByteBuffer> buffers, OutputStream out) throws IOException {
        byte[] chunk = null;
        for (ByteBuffer buffer : buffers) {
            ByteBuffer view = buffer.duplicate();
            if (view.hasArray()) {
                out.write(view.array(), view.arrayOffset() + view.position(), view.remaining());
                continue;
            }
            if (chunk == null) {
                chunk = new byte[CHUNK];
            }
            while (view.hasRemaining()) {
                int count = Math.min(chunk.length, view.remaining());
                view.get(chunk, 0, count);
                out.write(chunk, 0, count);
            }
        }
    }
}
