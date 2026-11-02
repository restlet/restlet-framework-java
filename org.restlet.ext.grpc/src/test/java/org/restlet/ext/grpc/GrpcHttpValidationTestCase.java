/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.OutputRepresentation;

class GrpcHttpValidationTestCase {
    private final GrpcClientAdapter adapter = new GrpcClientAdapter();
    private Server peer;
    private Client client;
    private static final MethodDescriptor<StringValue, StringValue> METHOD =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("review.Invalid/Call")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private void start(Restlet handler) throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().set("http.transport.protocol", "HTTP2");
        peer = new Server(serverContext, Protocol.HTTP, 0, handler);
        peer.start();
        Context clientContext = new Context();
        clientContext.getParameters().set("httpClientTransportMode", "HTTP2");
        clientContext.getParameters().set("followRedirects", "false");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
        adapter.setPlaintext(true);
        adapter.setClient(client);
        adapter.setTarget("localhost:" + peer.getActualPort());
        adapter.start();
    }

    private Status failure() {
        return assertThrows(
                        StatusRuntimeException.class,
                        () ->
                                ClientCalls.blockingUnaryCall(
                                        adapter.getChannel(),
                                        METHOD,
                                        CallOptions.DEFAULT,
                                        StringValue.of("request")))
                .getStatus();
    }

    private static byte[] frame(String value) {
        byte[] message = StringValue.of(value).toByteArray();
        return ByteBuffer.allocate(5 + message.length)
                .put((byte) 0)
                .putInt(message.length)
                .put(message)
                .array();
    }

    private void startMalformedThenHealthy(Consumer<Response> malformed) throws Exception {
        adapter.setMaxConcurrentCalls(1);
        AtomicInteger requests = new AtomicInteger();
        start(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (requests.incrementAndGet() == 1) {
                            malformed.accept(response);
                        } else {
                            response.setEntity(
                                    new ByteArrayRepresentation(
                                            frame("recovered"),
                                            MediaType.valueOf("application/grpc")));
                            response.getTrailers().add("grpc-status", "0");
                        }
                    }
                });
    }

    private void assertRecovered() {
        assertEquals(
                "recovered",
                ClientCalls.blockingUnaryCall(
                                adapter.getChannel(),
                                METHOD,
                                CallOptions.DEFAULT,
                                StringValue.of("next"))
                        .getValue());
        assertTrue(client.isStarted());
    }

    @AfterEach
    void cleanup() throws Exception {
        try {
            adapter.stop();
        } finally {
            try {
                if (client != null) {
                    client.stop();
                }
            } finally {
                if (peer != null) {
                    peer.stop();
                }
            }
        }
    }

    @Test
    void sdkMapsHttpErrorsAndNoPostIsReplayed() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        start(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        requests.incrementAndGet();
                        response.setStatus(org.restlet.data.Status.CLIENT_ERROR_UNAUTHORIZED);
                        response.setEntity("denied", MediaType.TEXT_PLAIN);
                    }
                });
        assertEquals(Status.Code.UNAUTHENTICATED, failure().getCode());
        assertEquals(1, requests.get());
    }

    @Test
    void missingGrpcStatusIsNotSuccess() throws Exception {
        startMalformedThenHealthy(
                response -> response.setEntity("", MediaType.valueOf("application/grpc")));
        Status status = failure();
        assertEquals(Status.Code.UNKNOWN, status.getCode(), status::toString);
        assertTrue(
                java.util.Objects.requireNonNull(status.getDescription())
                        .contains("missing GRPC status"));
        assertRecovered();
    }

    @Test
    void sdkRejectsTruncatedResponseFrame() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    new byte[] {0, 0, 0, 0, 10, 1},
                                    MediaType.valueOf("application/grpc")));
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.INTERNAL, failure().getCode());
        assertRecovered();
    }

    @Test
    void malformedBinaryMetadataFailsExplicitly() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    response.setEntity("", MediaType.valueOf("application/grpc"));
                    response.getHeaders().add("x-invalid-bin", "!not base64!");
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.UNAVAILABLE, failure().getCode());
        assertRecovered();
    }

    /**
     * The call slot must be free as soon as the caller is told that a call failed, otherwise the
     * next call of the same thread is rejected with RESOURCE_EXHAUSTED.
     */
    @Test
    void failedCallReleasesItsSlotBeforeTheCallerIsNotified() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        AtomicInteger requests = new AtomicInteger();
        start(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (requests.incrementAndGet() % 2 == 1) {
                            response.setEntity("", MediaType.valueOf("application/grpc"));
                        } else {
                            response.setEntity(
                                    new ByteArrayRepresentation(
                                            frame("recovered"),
                                            MediaType.valueOf("application/grpc")));
                            response.getTrailers().add("grpc-status", "0");
                        }
                    }
                });
        for (int i = 0; i < 200; i++) {
            int call = i;
            Status status = failure();
            // Only the slot matters here: depending on timing the failure is UNKNOWN or
            // UNAVAILABLE.
            assertNotEquals(Status.Code.OK, status.getCode(), () -> call + ": " + status);
            assertNotEquals(
                    Status.Code.RESOURCE_EXHAUSTED, status.getCode(), () -> call + ": " + status);
            assertRecovered();
        }
    }

    @Test
    void wrongContentTypeFailsAndRecovers() throws Exception {
        startMalformedThenHealthy(response -> response.setEntity("not grpc", MediaType.TEXT_PLAIN));
        assertEquals(Status.Code.UNKNOWN, failure().getCode());
        assertRecovered();
    }

    @Test
    void invalidFrameFlagsFailAndRecover() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    new byte[] {2, 0, 0, 0, 0},
                                    MediaType.valueOf("application/grpc")));
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.INTERNAL, failure().getCode());
        assertRecovered();
    }

    @Test
    void multipleUnaryResponseMessagesFailAndRecover() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    byte[] first = frame("one");
                    byte[] second = frame("two");
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    ByteBuffer.allocate(first.length + second.length)
                                            .put(first)
                                            .put(second)
                                            .array(),
                                    MediaType.valueOf("application/grpc")));
                    response.getTrailers().add("grpc-status", "0");
                });
        Status status = failure();
        assertEquals(Status.Code.CANCELLED, status.getCode(), status::toString);
        assertEquals(Status.Code.INTERNAL, Status.fromThrowable(status.getCause()).getCode());
        assertTrue(
                java.util.Objects.requireNonNull(
                                Status.fromThrowable(status.getCause()).getDescription())
                        .contains("More than one value"));
        assertRecovered();
    }

    @Test
    void successfulStatusWithoutUnaryMessageFailsAndRecovers() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    response.setEntity("", MediaType.valueOf("application/grpc"));
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.INTERNAL, failure().getCode());
        assertRecovered();
    }

    @Test
    void malformedStatusFailsAndRecovers() throws Exception {
        startMalformedThenHealthy(
                response -> {
                    response.setEntity("", MediaType.valueOf("application/grpc"));
                    response.getTrailers().add("grpc-status", "not-a-code");
                });
        assertEquals(Status.Code.UNKNOWN, failure().getCode());
        assertRecovered();
    }

    @Test
    void oversizedDeclaredMessageLengthFailsBeforePayloadAndRecovers() throws Exception {
        adapter.setMaxMessageBytes(128);
        startMalformedThenHealthy(
                response -> {
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    ByteBuffer.allocate(5).put((byte) 0).putInt(129).array(),
                                    MediaType.valueOf("application/grpc")));
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.RESOURCE_EXHAUSTED, failure().getCode());
        assertRecovered();
    }

    @Test
    void inboundWireBudgetRejectsOversizeResponseAndRecovers() throws Exception {
        adapter.setMaxMessageBytes(128);
        startMalformedThenHealthy(
                response -> {
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    frame("x".repeat(127)), MediaType.valueOf("application/grpc")));
                    response.getTrailers().add("grpc-status", "0");
                });
        assertEquals(Status.Code.RESOURCE_EXHAUSTED, failure().getCode());
        assertRecovered();
    }

    @Test
    void unsupportedEncodingAbortsAnUnfinishedResponseAndRecovers() throws Exception {
        CountDownLatch aborted = new CountDownLatch(1);
        startMalformedThenHealthy(
                response -> {
                    response.getHeaders().add("grpc-encoding", "unsupported-encoding");
                    response.setEntity(
                            new OutputRepresentation(MediaType.valueOf("application/grpc")) {
                                @Override
                                public void write(OutputStream output) throws IOException {
                                    try {
                                        for (int i = 0; i < 100; i++) {
                                            output.write(0);
                                            output.flush();
                                            Thread.sleep(100);
                                        }
                                    } catch (IOException e) {
                                        aborted.countDown();
                                        throw e;
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        throw new IOException(e);
                                    }
                                }
                            });
                });
        assertEquals(Status.Code.INTERNAL, failure().getCode());
        assertTrue(aborted.await(5, TimeUnit.SECONDS), "The HTTP exchange must be aborted");
        assertRecovered();
    }
}
