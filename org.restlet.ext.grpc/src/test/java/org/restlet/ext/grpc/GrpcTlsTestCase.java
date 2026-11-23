/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.StringValue;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.SecurityLevel;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/** Tests gRPC calls over TLS, on both adapters. */
class GrpcTlsTestCase {
    private static final String PASSWORD = "testtest";

    private static final MethodDescriptor<StringValue, StringValue> ECHO =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("review.Tls/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @TempDir Path tempDir;

    private final GrpcClientAdapter adapter = new GrpcClientAdapter();
    private final GrpcServerAdapter serverAdapter = new GrpcServerAdapter();
    private Client client;
    private io.grpc.Server peer;
    private Server http;
    private ManagedChannel channel;

    @AfterEach
    void cleanup() throws Exception {
        try {
            adapter.stop();
            if (channel != null) {
                channel.shutdownNow();
                assertTrue(channel.awaitTermination(30, TimeUnit.SECONDS));
            }
            serverAdapter.stop();
        } finally {
            if (client != null) {
                client.stop();
            }
            if (http != null) {
                http.stop();
            }
            if (peer != null) {
                peer.shutdownNow();
                assertTrue(peer.awaitTermination(30, TimeUnit.SECONDS));
            }
        }
    }

    private Path keystore() throws Exception {
        Path result = tempDir.resolve("dummy.p12");
        try (InputStream in = getClass().getResourceAsStream("dummy.p12")) {
            Files.copy(in, result, REPLACE_EXISTING);
        }
        return result;
    }

    private KeyStore loadKeystore() throws Exception {
        KeyStore result = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keystore())) {
            result.load(in, PASSWORD.toCharArray());
        }
        return result;
    }

    private static ServerServiceDefinition echo() {
        return ServerServiceDefinition.builder("review.Tls")
                .addMethod(
                        ECHO,
                        ServerCalls.asyncUnaryCall(
                                (StringValue request,
                                        io.grpc.stub.StreamObserver<StringValue> out) -> {
                                    out.onNext(request);
                                    out.onCompleted();
                                }))
                .build();
    }

    /** Records the authority, the authorization header and the transport of the calls. */
    private static final class Recorder implements ServerInterceptor {
        final AtomicReference<String> authority = new AtomicReference<>();
        final AtomicReference<String> authorization = new AtomicReference<>();
        final AtomicReference<io.grpc.Attributes> attributes = new AtomicReference<>();

        @Override
        public <Q, S> ServerCall.Listener<Q> interceptCall(
                ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
            authority.set(call.getAuthority());
            authorization.set(headers.get(AUTHORIZATION));
            attributes.set(call.getAttributes());
            return next.startCall(call, headers);
        }
    }

    /** Starts a native SDK server over TLS. */
    private void startPeer(ServerInterceptor interceptor) throws Exception {
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(loadKeystore(), PASSWORD.toCharArray());
        peer =
                NettyServerBuilder.forPort(0)
                        .sslContext(
                                GrpcSslContexts.configure(SslContextBuilder.forServer(kmf)).build())
                        .addService(echo())
                        .intercept(interceptor)
                        .build()
                        .start();
    }

    /** Starts an HTTPS client, trusting the test certificate when asked to. */
    private Client startClient(Protocol protocol, boolean trusting) throws Exception {
        Context context = new Context();
        context.getParameters().set("httpClientTransportMode", "HTTP2");
        context.getParameters().set("followRedirects", "false");
        if (trusting) {
            context.getParameters().set("truststorePath", keystore().toString());
            context.getParameters().set("truststorePassword", PASSWORD);
            context.getParameters().set("trustStoreType", "PKCS12");
        }
        client = new Client(context, protocol);
        client.start();
        return client;
    }

    private void startAdapter(String target) {
        adapter.setClient(client);
        adapter.setTarget(target);
        adapter.start();
    }

    private StringValue call(io.grpc.Channel on, CallOptions options, String value) {
        return ClientCalls.blockingUnaryCall(on, ECHO, options, StringValue.of(value));
    }

    @Test
    void clientCallsNativeServerOverTlsWithCallCredentials() throws Exception {
        Recorder recorder = new Recorder();
        startPeer(recorder);
        startClient(Protocol.HTTPS, true);
        startAdapter("dns:///localhost:" + peer.getPort());
        CallCredentials bearer =
                new CallCredentials() {
                    @Override
                    public void applyRequestMetadata(
                            RequestInfo info, Executor executor, MetadataApplier applier) {
                        if (info.getSecurityLevel() != SecurityLevel.PRIVACY_AND_INTEGRITY) {
                            applier.fail(
                                    Status.UNAUTHENTICATED.withDescription("Insecure transport"));
                            return;
                        }
                        Metadata headers = new Metadata();
                        headers.put(AUTHORIZATION, "Bearer secret");
                        applier.apply(headers);
                    }
                };

        assertEquals(
                "secure",
                call(
                                adapter.getChannel(),
                                CallOptions.DEFAULT.withCallCredentials(bearer),
                                "secure")
                        .getValue());
        assertEquals("localhost:" + peer.getPort(), recorder.authority.get());
        assertEquals("Bearer secret", recorder.authorization.get());
        assertNotNull(recorder.attributes.get().get(Grpc.TRANSPORT_ATTR_SSL_SESSION));
        assertEquals("localhost:" + peer.getPort(), adapter.getChannel().authority());
    }

    @Test
    void clientSendsStandardNamedMetadataAsIs() throws Exception {
        AtomicReference<Metadata> observed = new AtomicReference<>();
        startPeer(
                new ServerInterceptor() {
                    @Override
                    public <Q, S> ServerCall.Listener<Q> interceptCall(
                            ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
                        observed.set(headers);
                        return next.startCall(call, headers);
                    }
                });
        startClient(Protocol.HTTPS, true);
        startAdapter("localhost:" + peer.getPort());
        Metadata.Key<String> cookie = Metadata.Key.of("cookie", Metadata.ASCII_STRING_MARSHALLER);
        Metadata metadata = new Metadata();
        metadata.put(AUTHORIZATION, "api-key");
        metadata.put(AUTHORIZATION, "Bearer two");
        metadata.put(cookie, "a=1");
        var intercepted =
                io.grpc.ClientInterceptors.intercept(
                        adapter.getChannel(),
                        io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(metadata));
        assertEquals("raw", call(intercepted, CallOptions.DEFAULT, "raw").getValue());
        assertEquals(
                java.util.List.of("api-key", "Bearer two"),
                com.google.common.collect.ImmutableList.copyOf(
                        observed.get().getAll(AUTHORIZATION)));
        assertEquals("a=1", observed.get().get(cookie));
    }

    @Test
    void clientRequiresAnHttpClientForPlaintext() throws Exception {
        startClient(Protocol.HTTPS, false);
        adapter.setClient(client);
        adapter.setTarget("localhost:80");
        adapter.setPlaintext(true);
        IllegalStateException error = assertThrows(IllegalStateException.class, adapter::start);
        assertTrue(error.getMessage().contains("HTTP client"), error.getMessage());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(
            strings = {
                "localhost",
                "localhost:0",
                "localhost:65536",
                "::1:8443",
                "user@localhost:443",
                "dns:///:443",
                "localhost:443/path"
            })
    void clientRejectsInvalidTargets(String target) throws Exception {
        startClient(Protocol.HTTPS, false);
        adapter.setClient(client);
        adapter.setTarget(target);
        assertThrows(IllegalArgumentException.class, adapter::start);
    }

    @Test
    void clientAcceptsABracketedIpv6Target() throws Exception {
        startClient(Protocol.HTTPS, false);
        adapter.setClient(client);
        adapter.setTarget("dns:///[::1]:8443");
        adapter.start();
        assertEquals("[::1]:8443", adapter.getChannel().authority());
    }

    @Test
    void clientChecksTheCertificateAgainstTheTargetHost() throws Exception {
        startPeer(new Recorder());
        startClient(Protocol.HTTPS, true);
        // The test certificate is issued to localhost, not to its address.
        startAdapter("127.0.0.1:" + peer.getPort());
        StatusRuntimeException error =
                assertThrows(
                        StatusRuntimeException.class,
                        () -> call(adapter.getChannel(), CallOptions.DEFAULT, "ip"));
        assertEquals(Status.Code.UNAVAILABLE, error.getStatus().getCode());
    }

    @Test
    void clientRejectsAnUntrustedServer() throws Exception {
        startPeer(new Recorder());
        startClient(Protocol.HTTPS, false);
        startAdapter("localhost:" + peer.getPort());
        StatusRuntimeException error =
                assertThrows(
                        StatusRuntimeException.class,
                        () -> call(adapter.getChannel(), CallOptions.DEFAULT, "untrusted"));
        assertEquals(Status.Code.UNAVAILABLE, error.getStatus().getCode());
    }

    @Test
    void clientRequiresAnHttpsClientForTls() throws Exception {
        startClient(Protocol.HTTP, false);
        adapter.setClient(client);
        adapter.setTarget("localhost:443");
        IllegalStateException error = assertThrows(IllegalStateException.class, adapter::start);
        assertTrue(error.getMessage().contains("HTTPS"), error.getMessage());
    }

    /** Starts the server adapter on an HTTPS listener negotiating HTTP/2 with ALPN. */
    private void startServerAdapter(ServerInterceptor interceptor) throws Exception {
        serverAdapter.getServices().add(echo());
        serverAdapter.getInterceptors().add(interceptor);
        serverAdapter.start();
        Router router = new Router();
        serverAdapter.attachTo(router);
        Context context = new Context();
        context.getParameters().add("http.transport.protocols", "HTTP2,HTTP1_1");
        context.getParameters().add("keystorePath", keystore().toString());
        context.getParameters().add("keystorePassword", PASSWORD);
        context.getParameters().add("keyStoreType", "PKCS12");
        context.getParameters().add("keyPassword", PASSWORD);
        http = new Server(context, Protocol.HTTPS, 0, router);
        http.start();
    }

    @Test
    void serverAdapterServesNativeClientOverTls() throws Exception {
        Recorder recorder = new Recorder();
        startServerAdapter(recorder);
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(loadKeystore());
        channel =
                NettyChannelBuilder.forAddress("localhost", http.getActualPort())
                        .sslContext(GrpcSslContexts.forClient().trustManager(tmf).build())
                        .build();
        assertEquals("native", call(channel, CallOptions.DEFAULT, "native").getValue());
        assertEquals(
                SecurityLevel.PRIVACY_AND_INTEGRITY,
                recorder.attributes.get().get(io.grpc.internal.GrpcAttributes.ATTR_SECURITY_LEVEL));
    }

    @Test
    void adaptersInteroperateOverTls() throws Exception {
        Recorder recorder = new Recorder();
        startServerAdapter(recorder);
        startClient(Protocol.HTTPS, true);
        startAdapter("localhost:" + http.getActualPort());
        assertEquals(
                "restlet", call(adapter.getChannel(), CallOptions.DEFAULT, "restlet").getValue());
        assertEquals("localhost:" + http.getActualPort(), recorder.authority.get());
        assertEquals(
                SecurityLevel.PRIVACY_AND_INTEGRITY,
                recorder.attributes.get().get(io.grpc.internal.GrpcAttributes.ATTR_SECURITY_LEVEL));
    }
}
