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
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.util.Series;
import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.MessageExchange;
import software.amazon.smithy.java.client.core.error.TransportException;
import software.amazon.smithy.java.client.core.error.TransportSocketTimeout;
import software.amazon.smithy.java.client.http.HttpContext;
import software.amazon.smithy.java.client.http.HttpMessageExchange;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.http.api.HttpRequest;
import software.amazon.smithy.java.http.api.HttpResponse;
import software.amazon.smithy.java.http.api.HttpVersion;
import software.amazon.smithy.java.io.datastream.DataStream;

/**
 * smithy-java client transport sending SDK HTTP requests through a Restlet HTTP client.
 *
 * <p>Generated SDK clients and {@code DynamicClient}s keep their own protocols, serialization,
 * endpoints, auth schemes, signing, retries and modeled errors: they hand this transport finished
 * requests. Their method, raw target, headers (standard ones included) and body bytes are sent
 * unchanged, so signatures stay valid. Redirects are not followed, as with the SDK's own transport.
 * Response bodies are streamed, not buffered.
 *
 * <p>Built with a {@link Client}, the transport borrows it: the host starts it, stops it and may
 * share it, and the client must be configured with {@code followRedirects=false}. Built otherwise,
 * the transport owns an HTTP client and stops it on {@link #close()}. The transport is also
 * discovered under the name {@code restlet} by SDK clients built without a transport, in preference
 * to the SDK's {@code java.net.http} transport.
 *
 * <p>Event streams are not supported.
 */
public final class SmithyClientAdapter implements ClientTransport<HttpRequest, HttpResponse> {
    private final Client client;
    private final boolean owned;
    private volatile Duration requestTimeout;

    /** Constructor owning a default HTTP/1.1 client. */
    public SmithyClientAdapter() {
        this(new Context());
    }

    /**
     * Constructor owning an HTTP client created with the given context, for example with an {@code
     * httpClientTransportMode} parameter. Its {@code followRedirects} parameter is set to false.
     *
     * @param context The context of the owned client.
     */
    public SmithyClientAdapter(Context context) {
        Objects.requireNonNull(context, "context").getParameters().set("followRedirects", "false");
        this.client = new Client(context, Protocol.HTTP);
        this.owned = true;
        try {
            client.start();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start the Restlet HTTP client", e);
        }
    }

    /**
     * Constructor borrowing a client. The host starts and stops it.
     *
     * @param client The borrowed client, configured with {@code followRedirects=false}.
     */
    public SmithyClientAdapter(Client client) {
        Objects.requireNonNull(client, "client");
        if (client.getContext() == null
                || !"false"
                        .equalsIgnoreCase(
                                client.getContext()
                                        .getParameters()
                                        .getFirstValue("followRedirects", "true"))) {
            // A redirect would be sent again without the SDK, its signature and its retries.
            throw new IllegalArgumentException("Borrow a client with followRedirects=false");
        }
        this.client = client;
        this.owned = false;
    }

    /**
     * Returns the Restlet client, borrowed or owned.
     *
     * @return The Restlet client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Indicates whether the transport owns its client and stops it when closed.
     *
     * @return True if the transport owns its client.
     */
    public boolean isClientOwned() {
        return owned;
    }

    /**
     * Returns the default time allowed to receive the response headers of a call, or null when
     * unlimited.
     *
     * @return The default request timeout, or null.
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the default time allowed to receive the response headers of a call. The {@link
     * HttpContext#HTTP_REQUEST_TIMEOUT} setting of a call takes precedence. On expiry, the request
     * is aborted and the call fails with a {@link TransportSocketTimeout}.
     *
     * @param value The default request timeout, or null when unlimited.
     */
    public void setRequestTimeout(Duration value) {
        if (value != null && (value.isNegative() || value.isZero())) {
            throw new IllegalArgumentException("A positive request timeout is required");
        }
        requestTimeout = value;
    }

    @Override
    public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
        return HttpMessageExchange.INSTANCE;
    }

    /**
     * Sends a request and returns its response once the headers are received. The body stays a
     * stream. Thread interruption and timeouts abort the request and fail the call with a {@link
     * TransportException}; connector errors are mapped like the SDK maps I/O errors.
     */
    @Override
    public HttpResponse send(software.amazon.smithy.java.context.Context context, HttpRequest req) {
        Request request;
        try {
            request = toRestlet(req);
        } catch (RuntimeException e) {
            throw ClientTransport.remapExceptions(e);
        }
        Duration timeout = context.get(HttpContext.HTTP_REQUEST_TIMEOUT);
        if (timeout == null) {
            timeout = requestTimeout;
        }
        // The exchange runs on its own thread, so that the caller can give up on it.
        CompletableFuture<Response> exchange = new CompletableFuture<>();
        Thread.ofVirtual()
                .name("smithy-restlet-call")
                .start(
                        () -> {
                            try {
                                exchange.complete(client.handle(request));
                            } catch (Throwable e) {
                                exchange.completeExceptionally(e);
                            }
                        });
        Response response;
        try {
            response =
                    timeout == null
                            ? exchange.get()
                            : exchange.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            abandon(request, exchange);
            throw new TransportSocketTimeout("No response received within " + timeout, e);
        } catch (InterruptedException e) {
            abandon(request, exchange);
            Thread.currentThread().interrupt();
            throw new TransportException("Interrupted while waiting for the response", e);
        } catch (ExecutionException e) {
            throw ClientTransport.remapExceptions(e.getCause());
        }
        return toSmithy(response);
    }

    private static Request toRestlet(HttpRequest req) {
        // The URI keeps the target exactly as serialized, percent-encoding included.
        Request request = new Request(Method.valueOf(req.method()), req.uri().toString());
        String type = req.headers().contentType();
        request.setEntity(
                RequestBody.of(req.body(), type == null ? null : MediaType.valueOf(type)));
        // Written as serialized, and signed, by the SDK: standard headers included.
        var raw = new Series<Header>(Header.class);
        req.headers().forEachEntry(raw::add);
        request.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_REQUEST_HEADERS, raw);
        return request;
    }

    private static HttpResponse toSmithy(Response response) {
        Status status = response.getStatus();
        if (status.isConnectorError()) {
            Throwable cause = status.getThrowable();
            throw ClientTransport.remapExceptions(
                    cause == null ? new IOException(status.toString()) : cause);
        }
        var headers = HttpHeaders.ofModifiable();
        for (Header header : response.getHeaders()) {
            headers.addHeader(header.getName(), header.getValue());
        }
        Representation entity = response.getEntity();
        DataStream body;
        try {
            body =
                    entity == null || !entity.isAvailable()
                            ? DataStream.ofEmpty()
                            : DataStream.ofInputStream(
                                    entity.getStream(), headers.contentType(), entity.getSize());
        } catch (IOException e) {
            throw ClientTransport.remapExceptions(e);
        }
        HttpVersion version =
                response.getProtocol() != null && "2.0".equals(response.getProtocol().getVersion())
                        ? HttpVersion.HTTP_2
                        : HttpVersion.HTTP_1_1;
        return HttpResponse.of(version, status.getCode(), headers, body);
    }

    /** Aborts an exchange the caller gave up on, and releases its response if one arrives. */
    private static void abandon(Request request, CompletableFuture<Response> exchange) {
        request.abort();
        exchange.thenAccept(
                response -> {
                    if (response.getEntity() != null) {
                        response.getEntity().release();
                    }
                });
    }

    /** Stops the client if the transport owns it. A borrowed client is left running. */
    @Override
    public void close() {
        if (owned) {
            try {
                client.stop();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to stop the Restlet HTTP client", e);
            }
        }
    }
}
