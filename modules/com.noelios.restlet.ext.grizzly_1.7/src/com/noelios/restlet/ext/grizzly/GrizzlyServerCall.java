/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.grizzly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.restlet.Server;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.util.ChunkedInputStream;
import com.sun.grizzly.util.ByteBufferInputStream;
import com.sun.grizzly.util.OutputWriter;
import com.sun.grizzly.util.SSLOutputWriter;

/**
 * HTTP server call specialized for Grizzly.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class GrizzlyServerCall extends HttpServerCall {

    /** The NIO byte buffer. */
    private final ByteBuffer byteBuffer;

    /** Recycled stream. */
    private final ByteBufferInputStream requestStream;

    /** The underlying socket channel. */
    private final SocketChannel socketChannel;

    /**
     * Constructor.
     * 
     * @param server
     *                The parent server.
     * @param byteBuffer
     *                The NIO byte buffer.
     * @param key
     *                The NIO selection key.
     * @param confidential
     *                Indicates if the call is confidential.
     */
    public GrizzlyServerCall(Server server, ByteBuffer byteBuffer,
            SelectionKey key, boolean confidential) {
        super(server);
        setConfidential(confidential);

        this.byteBuffer = byteBuffer;
        this.requestStream = new ByteBufferInputStream();
        this.requestStream.setSelectionKey(key);
        this.requestStream.setByteBuffer(byteBuffer);
        this.socketChannel = (SocketChannel) key.channel();

        this.getRequestHeaders().clear();

        try {
            // Read the request header
            readRequestHead(requestStream);
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
                    ioe);
        }
    }

    @Override
    public String getClientAddress() {
        return socketChannel.socket().getInetAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        return socketChannel.socket().getPort();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        if (isRequestChunked()) {
            // Leave chunked encoding to the stream mode
            return null;
        } else {
            return new ReadableEntityChannel(this.byteBuffer,
                    getSocketChannel(), size);
        }
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (isRequestChunked()) {
            return new ChunkedInputStream(this.requestStream);
        } else {
            // Leave normal encoding to the channel mode
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        return getSocketChannel();
    }

    @Override
    public InputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        return getWritableChannel();
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return null;
    }

    /**
     * Returns the readable socket channel.
     * 
     * @return The readable socket channel.
     */
    private SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    @Override
    public String getSslCipherSuite() {
        Socket socket = this.socketChannel.socket();
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession sslSession = sslSocket.getSession();
            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }
        return null;
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        Socket socket = this.socketChannel.socket();
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession sslSession = sslSocket.getSession();
            if (sslSession != null) {
                try {
                    List<Certificate> clientCertificates = Arrays
                            .asList(sslSession.getPeerCertificates());

                    return clientCertificates;
                } catch (SSLPeerUnverifiedException e) {
                    getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }
        return null;
    }

    /**
     * Return the underlying socket channel.
     * 
     * @return The underlying socket channel.
     */
    public WritableByteChannel getWritableChannel() {
        if (isConfidential()) {
            return new WritableByteChannel() {
                public void close() throws IOException {
                    getSocketChannel().close();
                }

                public boolean isOpen() {
                    return getSocketChannel().isOpen();
                }

                public int write(ByteBuffer src) throws IOException {
                    int nWrite = src.limit();
                    SSLOutputWriter.flushChannel(socketChannel, src);
                    return nWrite;
                }
            };
        } else {
            return this.socketChannel;
        }
    }

    @Override
    public void writeResponseBody(Representation entity) throws IOException {
        entity.write(getResponseEntityChannel());
    }

    @Override
    public void writeResponseHead(Response response) throws IOException {
        ByteArrayOutputStream headStream = new ByteArrayOutputStream(8192);
        writeResponseHead(headStream);
        ByteBuffer buffer = ByteBuffer.wrap(headStream.toByteArray());

        if (isConfidential()) {
            SSLOutputWriter.flushChannel(socketChannel, buffer);
        } else {
            OutputWriter.flushChannel(socketChannel, buffer);
        }

        buffer.clear();
    }
}
