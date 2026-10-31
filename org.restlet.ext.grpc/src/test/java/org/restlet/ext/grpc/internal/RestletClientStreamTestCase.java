/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.grpc.CallOptions;
import io.grpc.ClientStreamTracer;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.internal.ClientStreamListener;
import io.grpc.internal.StreamListener.MessageProducer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.data.Protocol;

class RestletClientStreamTestCase {
    @Test
    void cancelledTimersDoNotStayQueued() {
        var factory = new RestletTransportFactory(null, 1024, 1, java.time.Duration.ofSeconds(1));
        try {
            var timer = (ScheduledThreadPoolExecutor) factory.getScheduledExecutorService();
            for (int i = 0; i < 100; i++) {
                timer.schedule(() -> {}, 1, TimeUnit.HOURS).cancel(false);
            }
            assertEquals(0, timer.getQueue().size());
        } finally {
            factory.close();
        }
    }

    @Test
    void shutdownBeforeListenerInstallationIsReplayedOnceAndReleasesAdmission() {
        var marshaller =
                new MethodDescriptor.Marshaller<byte[]>() {
                    @Override
                    public InputStream stream(byte[] value) {
                        return new ByteArrayInputStream(value);
                    }

                    @Override
                    public byte[] parse(InputStream input) {
                        throw new UnsupportedOperationException("No response is expected");
                    }
                };
        var method =
                MethodDescriptor.<byte[], byte[]>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName("review.Unary/Echo")
                        .setRequestMarshaller(marshaller)
                        .setResponseMarshaller(marshaller)
                        .build();
        AtomicInteger released = new AtomicInteger();
        AtomicInteger terminals = new AtomicInteger();
        AtomicReference<Status> status = new AtomicReference<>();
        try (var io = Executors.newVirtualThreadPerTaskExecutor()) {
            var stream =
                    new RestletClientStream(
                            new Client(Protocol.HTTP),
                            new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080),
                            "localhost:8080",
                            method,
                            new Metadata(),
                            CallOptions.DEFAULT,
                            new ClientStreamTracer[0],
                            128,
                            io,
                            ignored -> released.incrementAndGet());
            Status shutdown = Status.UNAVAILABLE.withDescription("shutdown before start");
            stream.cancel(shutdown);
            stream.cancel(Status.CANCELLED);
            assertEquals(0, terminals.get());
            assertEquals(0, released.get());
            new GuardedClientStream(stream, 128)
                    .start(
                            new ClientStreamListener() {
                                @Override
                                public void headersRead(Metadata headers) {}

                                @Override
                                public void closed(
                                        Status value, RpcProgress progress, Metadata trailers) {
                                    status.set(value);
                                    terminals.incrementAndGet();
                                }

                                @Override
                                public void messagesAvailable(MessageProducer producer) {}

                                @Override
                                public void onReady() {}
                            });
            stream.cancel(Status.CANCELLED);
            assertSame(shutdown, status.get());
            assertEquals(1, terminals.get());
            assertEquals(1, released.get());
        }
    }
}
