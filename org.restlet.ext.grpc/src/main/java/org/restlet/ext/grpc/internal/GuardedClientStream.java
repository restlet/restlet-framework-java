/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc.internal;

import io.grpc.Attributes;
import io.grpc.Compressor;
import io.grpc.Deadline;
import io.grpc.DecompressorRegistry;
import io.grpc.Status;
import io.grpc.internal.ClientStream;
import io.grpc.internal.ClientStreamListener;
import io.grpc.internal.InsightBuilder;
import java.io.InputStream;

/** Delayed SDK streams must turn synchronous framing failures into terminal call status. */
final class GuardedClientStream implements ClientStream {
    private final ClientStream delegate;
    private final int maxBytes;
    private int maxOutboundBytes;

    GuardedClientStream(ClientStream delegate, int maxBytes) {
        this.delegate = delegate;
        this.maxBytes = maxBytes;
        maxOutboundBytes = maxBytes;
    }

    @Override
    public void writeMessage(InputStream message) {
        try {
            delegate.writeMessage(message);
        } catch (RuntimeException e) {
            delegate.cancel(Status.fromThrowable(e));
        }
    }

    @Override
    public void cancel(Status reason) {
        delegate.cancel(reason);
    }

    @Override
    public void halfClose() {
        delegate.halfClose();
    }

    @Override
    public void setAuthority(String authority) {
        delegate.setAuthority(authority);
    }

    @Override
    public void setFullStreamDecompression(boolean enabled) {
        delegate.setFullStreamDecompression(enabled);
    }

    @Override
    public void setDecompressorRegistry(DecompressorRegistry registry) {
        delegate.setDecompressorRegistry(registry);
    }

    @Override
    public void start(ClientStreamListener listener) {
        // The SDK framer accepts this limit only once, after per-call options are applied.
        delegate.setMaxOutboundMessageSize(maxOutboundBytes);
        delegate.start(listener);
    }

    @Override
    public void setMaxInboundMessageSize(int size) {
        delegate.setMaxInboundMessageSize(Math.min(size, maxBytes));
    }

    @Override
    public void setMaxOutboundMessageSize(int size) {
        maxOutboundBytes = Math.min(size, maxBytes);
    }

    @Override
    public void setDeadline(Deadline deadline) {
        delegate.setDeadline(deadline);
    }

    @Override
    public Attributes getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public void appendTimeoutInsight(InsightBuilder insight) {
        delegate.appendTimeoutInsight(insight);
    }

    @Override
    public void request(int count) {
        delegate.request(count);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public void optimizeForDirectExecutor() {
        delegate.optimizeForDirectExecutor();
    }

    @Override
    public void setCompressor(Compressor compressor) {
        delegate.setCompressor(compressor);
    }

    @Override
    public void setMessageCompression(boolean enabled) {
        delegate.setMessageCompression(enabled);
    }
}
