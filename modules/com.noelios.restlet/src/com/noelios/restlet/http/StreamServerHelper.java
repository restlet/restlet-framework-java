/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * HTTP server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StreamServerHelper extends HttpServerHelper {

    /** The connection handler service. */
    private ExecutorService handlerService;

    /** The socket listener service. */
    private ExecutorService listenerService;

    /** The server socket channel. */
    private ServerSocketChannel serverSocketChannel;

    /** The synchronization aid between listener and handler service. */
    private CountDownLatch latch;

    /**
     * Constructor.
     * 
     * @param server
     *                The server to help.
     */
    public StreamServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getLogger().info("Starting the internal HTTP server");

        // Configure the thread services
        handlerService = Executors.newFixedThreadPool(10);
        listenerService = Executors.newSingleThreadExecutor();

        // Create the server socket
        serverSocketChannel = createServerSocket();

        // Start the socket listener service
        latch = new CountDownLatch(1);
        listenerService.submit(new Listener(this, serverSocketChannel, latch,
                handlerService));

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            latch.await();
        } catch (InterruptedException ex) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the internal HTTP server");

        if (handlerService != null) {
            // Gracefully shutdown the handlers, they should complete
            // in a timely fashion
            handlerService.shutdown();
            try {
                handlerService.awaitTermination(Long.MAX_VALUE,
                        TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (listenerService != null) {
            // This must be forcefully interrupted because the thread
            // is most likely blocked on channel.accept()
            listenerService.shutdownNow();

            try {
                listenerService.awaitTermination(Long.MAX_VALUE,
                        TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Close the server socket
        if (serverSocketChannel != null) {
            serverSocketChannel.close();
        }
    }

    /**
     * Create a server socket channel and bind it to the given address
     * 
     * @return Bound server socket channel.
     * @throws IOException
     */
    protected ServerSocketChannel createServerSocket() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(createSocketAddress());
        return server;
    }

    /**
     * Creates a socket address to listen on.
     * 
     * @return The created socket address.
     * @throws IOException
     */
    protected SocketAddress createSocketAddress() throws IOException {
        if (getServer().getAddress() == null) {
            return new InetSocketAddress(getServer().getPort());
        } else {
            return new InetSocketAddress(getServer().getAddress(), getServer()
                    .getPort());
        }
    }

    /**
     * Class that handles an incoming socket.
     */
    private static class ConnectionHandler implements Runnable {

        /** The target server helper. */
        private final StreamServerHelper helper;

        /** The socket connection to handle. */
        private final Socket socket;

        /**
         * Constructor.
         * 
         * @param helper
         *                The target server helper.
         * @param socket
         *                The socket connection to handle.
         */
        private ConnectionHandler(StreamServerHelper helper, Socket socket) {
            this.helper = helper;
            this.socket = socket;
        }

        /**
         * Handles the given socket connection.
         */
        public void run() {
            try {
                helper.handle(new StreamServerCall(helper.getServer(), socket
                        .getInputStream(), socket.getOutputStream()));
            } catch (IOException ex) {
                helper.getLogger().log(Level.WARNING,
                        "Unexpected error while handling a call", ex);
            }
        }
    }

    /**
     * Listens on the given socket channel for incoming connections and
     * dispatches them to the given handler pool
     */
    private static class Listener implements Runnable {

        /** The target server helper. */
        private final StreamServerHelper helper;

        /** The server socket channel to listen on. */
        private final ServerSocketChannel serverSocket;

        /**
         * The latch to countdown when the socket is ready to accept
         * connections.
         */
        private final CountDownLatch latch;

        /** The handler service. */
        private final ExecutorService handlerService;

        /**
         * Constructor.
         * 
         * @param helper
         *                The target server helper.
         * @param serverSocket
         *                The server socket channel to listen on.
         * @param latch
         *                The latch to countdown when the socket is ready to
         *                accept connections.
         * @param handlerService
         *                The handler service.
         */
        private Listener(StreamServerHelper helper,
                ServerSocketChannel serverSocket, CountDownLatch latch,
                ExecutorService handlerService) {
            this.helper = helper;
            this.serverSocket = serverSocket;
            this.latch = latch;
            this.handlerService = handlerService;
        }

        /**
         * Listens on the given server socket for incoming connections.
         */
        public void run() {
            latch.countDown();
            for (;;) {
                try {
                    SocketChannel client = (SocketChannel) serverSocket
                            .accept();
                    if (!handlerService.isShutdown()) {
                        handlerService.submit(new ConnectionHandler(helper,
                                client.socket()));
                    }
                } catch (ClosedByInterruptException ex) {
                    break;
                } catch (IOException ex) {
                    helper.getLogger().log(Level.WARNING,
                            "Unexpected error while accepting new connection",
                            ex);
                }
            }
        }
    }
}
