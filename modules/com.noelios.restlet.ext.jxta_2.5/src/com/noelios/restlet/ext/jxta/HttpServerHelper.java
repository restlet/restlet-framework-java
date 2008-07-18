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

package com.noelios.restlet.ext.jxta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jxta.socket.JxtaServerSocket;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Concrete JXTA-based HTTP server connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpServerHelper extends JxtaServerHelper {

    private static class ServerSocketChannelWrapper extends ServerSocketChannel {

        private final ServerSocket socket;

        public ServerSocketChannelWrapper(ServerSocket socket) {
            super(null);
            this.socket = socket;
        }

        @Override
        public SocketChannel accept() throws IOException {
            return new SocketChannelWrapper(this.socket.accept());
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
            this.socket.close();
        }

        @Override
        protected void implConfigureBlocking(boolean block) throws IOException {

        }

        @Override
        public ServerSocket socket() {
            return this.socket;
        }
    }

    private static class SocketChannelWrapper extends SocketChannel {
        private final Socket socket;

        public SocketChannelWrapper(Socket socket) {
            super(null);
            this.socket = socket;
        }

        @Override
        public boolean connect(SocketAddress remote) throws IOException {
            return false;
        }

        @Override
        public boolean finishConnect() throws IOException {
            return false;
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
            this.socket.close();
        }

        @Override
        protected void implConfigureBlocking(boolean block) throws IOException {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public boolean isConnectionPending() {
            return false;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return 0;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length)
                throws IOException {
            return 0;
        }

        @Override
        public Socket socket() {
            return this.socket;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return 0;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            return 0;
        }
    }

    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    protected ServerSocketChannel createServerSocket() throws IOException {
        final ServerSocket serverSocket = new JxtaServerSocket(getPeerGroup(),
                getPipeAdvertisement());
        serverSocket.setSoTimeout(0);
        return new ServerSocketChannelWrapper(serverSocket);
    }

    @Override
    protected SocketAddress createSocketAddress() throws IOException {
        return null;
    }
}
