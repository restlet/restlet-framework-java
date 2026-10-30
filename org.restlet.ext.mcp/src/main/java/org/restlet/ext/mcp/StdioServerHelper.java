/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ServerHelper;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;

/**
 * Explicit newline-framed MCP STDIO connector. Every message traverses the Server's configured
 * Restlet chain. The owner must supply a StreamTransport; stdout is exclusively protocol output.
 */
public final class StdioServerHelper extends ServerHelper {
    private final Set<Request> requests = ConcurrentHashMap.newKeySet();
    private org.restlet.util.StreamTransport stream;
    private McpWorkers workers;
    private Thread reader;
    private volatile boolean closed;
    private volatile Throwable failure;

    public StdioServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.STDIO);
    }

    @Override
    public synchronized void start() throws IOException {
        if (reader != null) {
            throw new IllegalStateException("MCP STDIO connector is single-use");
        }
        Object value = getContext().getAttributes().get(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE);
        if (!(value instanceof org.restlet.util.StreamTransport configured)) {
            throw new IllegalStateException(
                    "MCP STDIO requires an explicitly configured StreamTransport");
        }
        stream = configured;
        stream.setMaxOperationBytes(McpProtocol.DEFAULT_MAX_MESSAGE_BYTES + 1);
        stream.start();
        workers = new McpWorkers(McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS, Duration.ofSeconds(30));
        reader = Thread.ofVirtual().name("restlet-mcp-stdio-server").start(this::read);
    }

    private void read() {
        try {
            byte[] bytes;
            while (!closed
                    && (bytes =
                                    McpMessages.readFrame(
                                            stream, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES))
                            != null) {
                byte[] frame = bytes;
                workers.submit(() -> dispatch(frame), () -> {})
                        .subscribe(ignored -> {}, this::fail);
            }
            if (!closed) {
                fail(new IOException("MCP STDIO input ended"));
            }
        } catch (IOException | RuntimeException e) {
            if (!closed) {
                fail(e);
            }
        }
    }

    private void fail(Throwable cause) {
        failure = cause;
        closed = true;
        getLogger().log(Level.WARNING, "MCP STDIO server input/exchange failed", cause);
        requests.forEach(Request::abort);
        try {
            stream.stop();
        } catch (IOException e) {
            cause.addSuppressed(e);
            getLogger().log(Level.WARNING, "Unable to stop failed MCP STDIO stream", e);
        }
    }

    private void dispatch(byte[] bytes) throws IOException {
        if (closed) {
            throw new IOException("MCP STDIO server is closed", failure);
        }
        Request request =
                new Request(
                        Method.POST,
                        "stdio://localhost/mcp",
                        new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.VERSION);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        Response response = new Response(request);
        requests.add(request);
        try {
            if (closed) {
                request.abort();
                throw new IOException("MCP STDIO server is closed", failure);
            }
            handle(request, response);
            if (response.getEntity() != null) {
                if (!MediaType.APPLICATION_JSON.isCompatible(response.getEntity().getMediaType())) {
                    throw new IOException("MCP STDIO handler returned a non-JSON entity");
                }
                try (var input = response.getEntity().getStream()) {
                    McpMessages.writeFrame(
                            stream, McpMessages.read(input, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES));
                }
            } else if (!response.getStatus().isSuccess()) {
                throw new IOException(
                        "MCP STDIO handler rejected exchange: " + response.getStatus());
            }
        } finally {
            requests.remove(request);
            if (response.getEntity() != null) {
                response.getEntity().release();
            }
        }
    }

    /**
     * Terminal input/worker failure, if any; connector owners should monitor this for subprocesses.
     */
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public synchronized void stop() throws Exception {
        closed = true;
        requests.forEach(Request::abort);
        Exception error = null;
        try {
            if (stream != null) {
                stream.stop();
            }
        } catch (IOException e) {
            error = e;
        }
        try {
            if (workers != null) {
                workers.close();
            }
        } catch (IOException e) {
            if (error == null) {
                error = e;
            } else {
                error.addSuppressed(e);
            }
        }
        if (reader != null && !reader.join(Duration.ofSeconds(30))) {
            IOException join = new IOException("MCP STDIO server reader shutdown timed out");
            if (error == null) {
                error = join;
            } else {
                error.addSuppressed(join);
            }
        }
        super.stop();
        if (error != null) {
            throw error;
        }
    }
}
