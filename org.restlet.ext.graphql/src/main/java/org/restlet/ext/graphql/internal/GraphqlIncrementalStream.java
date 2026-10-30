/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql.internal;

import graphql.incremental.DelayedIncrementalPartialResult;
import graphql.incremental.IncrementalExecutionResult;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.representation.MultipartStreamRepresentation.PartSink;
import org.restlet.representation.MultipartStreamRepresentation.PartWriter;
import org.restlet.util.Series;

/**
 * Writes an incremental result as {@code multipart/mixed} parts: the initial result, then each
 * delayed result as it completes. Delayed results are requested one at a time, once the previous
 * part is written. If they fail, the response is aborted, so that clients don't mistake a partial
 * result for a complete one.
 */
public final class GraphqlIncrementalStream implements PartWriter {

    /** The media type of incremental responses, in the format of graphql-java. */
    public static final MediaType MEDIA_TYPE =
            new MediaType(MediaType.MULTIPART_MIXED, "deferSpec", "20220824");

    /** The boundary of incremental responses, as in the GraphQL incremental delivery RFC. */
    public static final String BOUNDARY = "-";

    private final IncrementalExecutionResult initial;

    private final long maxQueuedBytes;

    private final Logger logger;

    /**
     * Constructor.
     *
     * @param initial The initial result.
     * @param maxQueuedBytes The maximum number of bytes of delayed results not yet written.
     * @param logger The logger of failures.
     */
    public GraphqlIncrementalStream(
            IncrementalExecutionResult initial, long maxQueuedBytes, Logger logger) {
        this.initial = initial;
        this.maxQueuedBytes = maxQueuedBytes;
        this.logger = logger;
    }

    private static Series<Header> headers() {
        Series<Header> headers = new Series<>(Header.class);
        headers.add(new Header("Content-Type", "application/json; charset=utf-8"));
        return headers;
    }

    @Override
    public void write(PartSink sink) throws IOException {
        sink.send(headers(), GraphqlJson.write(initial.toSpecification()));
        if (!initial.hasNext()) {
            return;
        }

        GraphqlResultQueue queue =
                new GraphqlResultQueue(
                        maxQueuedBytes,
                        item -> ((DelayedIncrementalPartialResult) item).toSpecification());
        try {
            initial.getIncrementalItemPublisher().subscribe(queue);
            while (true) {
                GraphqlResultQueue.Signal signal = queue.take();
                if (signal instanceof GraphqlResultQueue.Next next) {
                    sink.send(headers(), next.json().getBytes(StandardCharsets.UTF_8));
                    queue.request();
                } else if (signal instanceof GraphqlResultQueue.Failure failure) {
                    logger.log(
                            Level.WARNING, "GraphQL incremental delivery failed", failure.cause());
                    throw new IOException(
                            "GraphQL incremental delivery failed: " + failure.message());
                } else {
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Incremental delivery interrupted");
        } finally {
            queue.cancel();
        }
    }
}
