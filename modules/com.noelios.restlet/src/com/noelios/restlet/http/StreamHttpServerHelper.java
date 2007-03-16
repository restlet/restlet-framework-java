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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * HTTP server helper based on BIO sockets.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StreamHttpServerHelper extends HttpServerHelper {
    /** The server socket to listen on. */
    private ServerSocket serverSocket;

    /** The server socket address. */
    private SocketAddress socketAddress;

    /** The executor service (thread pool). */
    private ExecutorService executorService;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public StreamHttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getLogger().info("Starting the HTTP server");
        new Listener(this).start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the HTTP server");
        if (this.serverSocket.isBound()) {
            this.serverSocket.close();
            this.serverSocket = null;
        }

        if (this.executorService != null) {
            this.executorService.shutdown();
        }
    }

    /**
     * Listener thread that accepts incoming requests.
     */
    class Listener extends Thread {
        private StreamHttpServerHelper helper;

        Listener(StreamHttpServerHelper helper) {
            this.helper = helper;
            setDaemon(true);
        }

        public void run() {
            try {
                if (socketAddress == null) {
                    if (getServer().getAddress() == null) {
                        socketAddress = new InetSocketAddress(getServer()
                                .getPort());
                    } else {
                        socketAddress = new InetSocketAddress(getServer()
                                .getAddress(), getServer().getPort());
                    }
                }

                executorService = Executors.newFixedThreadPool(10);
                serverSocket = new ServerSocket();
                serverSocket.bind(socketAddress);

                for (;;) {
                    executorService.execute(new Connection(helper, serverSocket
                            .accept()));
                }
            } catch (IOException ioe) {
                try {
                    this.helper.stop();
                } catch (Exception e) {
                    getLogger().log(Level.WARNING,
                            "Unexpected error while stopping the connector", e);
                }
            }
        }
    }

    /**
     * Connection that handles the socket.
     */
    class Connection implements Runnable {
        private StreamHttpServerHelper helper;

        private final Socket socket;

        Connection(StreamHttpServerHelper helper, Socket socket) {
            this.helper = helper;
            this.socket = socket;
        }

        public void run() {
            try {
                this.helper.handle(new StreamHttpServerCall(this.helper
                        .getServer(), this.socket.getInputStream(), this.socket
                        .getOutputStream()));
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unexpected error while handle a call", ioe);
            }
        }
    }
}
