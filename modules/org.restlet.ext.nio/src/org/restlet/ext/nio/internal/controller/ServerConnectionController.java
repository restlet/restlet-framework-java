/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.nio.internal.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.nio.ServerConnectionHelper;
import org.restlet.ext.nio.internal.connection.Connection;

/**
 * Controls the IO work of parent server helper and manages its connections.
 * Listens on a server socket channel for incoming connections.
 * 
 * @author Jerome Louvel
 */
public class ServerConnectionController extends ConnectionController {

    /** The latch to countdown when the socket is ready to accept connections. */
    private final CountDownLatch latch;

    /**
     * Constructor.
     * 
     * @param helper
     *            The target server helper.
     */
    public ServerConnectionController(ServerConnectionHelper helper) {
        super(helper);
        this.latch = new CountDownLatch(1);
    }

    /**
     * Awaits for the controller to be effectively started.
     * 
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        if (!this.latch.await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            // Timeout detected
            getHelper()
                    .getLogger()
                    .warning(
                            "The calling thread timed out while waiting for the controller to be ready to accept connections.");
        }
    }

    @Override
    protected void doInit() {
        super.doInit();

        // Register interest in NIO accept events
        try {
            getHelper().getServerSocketChannel().register(getSelector(),
                    SelectionKey.OP_ACCEPT);
        } catch (IOException ioe) {
            getHelper().getLogger().log(Level.WARNING,
                    "Unexpected error while registering an NIO selection key",
                    ioe);
        }

        this.latch.countDown();
    }

    /**
     * Returns the parent server helper.
     * 
     * @return The parent server helper.
     */
    protected ServerConnectionHelper getHelper() {
        return (ServerConnectionHelper) super.getHelper();
    }

    @Override
    protected void onSelected(SelectionKey key) throws IOException {
        if (!key.isAcceptable()) {
            super.onSelected(key);
        } else if (!isOverloaded()) {
            try {
                // Accept the new connection
                SocketChannel socketChannel = getHelper()
                        .getServerSocketChannel().accept();

                if (socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    getHelper().configure(socketChannel.socket());

                    int connectionsCount = getHelper().getConnections().size();

                    if ((getHelper().getMaxTotalConnections() == -1)
                            || (connectionsCount <= getHelper()
                                    .getMaxTotalConnections())) {
                        Connection<Server> connection = getHelper().checkout(
                                socketChannel,
                                this,
                                (InetSocketAddress) socketChannel.socket()
                                        .getRemoteSocketAddress());
                        connection.open();
                        getHelper().getConnections().add(connection);

                        if (getHelper().getLogger().isLoggable(Level.FINE)) {
                            getHelper().getLogger().fine(
                                    "Connection from \""
                                            + connection.getSocketAddress()
                                            + "\" accepted. New count: "
                                            + getHelper().getConnections()
                                                    .size());
                        }
                    } else {
                        // Rejection connection
                        socketChannel.close();
                        getHelper()
                                .getLogger()
                                .info("Maximum number of concurrent connections reached. New connection rejected.");
                    }
                }
            } catch (ClosedByInterruptException ex) {
                getHelper().getLogger().log(Level.FINE,
                        "ServerSocket channel was closed by interrupt", ex);
                throw ex;
            } catch (AsynchronousCloseException ace) {
                getHelper().getLogger().log(Level.FINE,
                        "The server socket was closed", ace);
            } catch (SocketException se) {
                getHelper().getLogger().log(Level.FINE,
                        "The server socket was closed", se);
            } catch (IOException ex) {
                getHelper().getLogger().log(Level.WARNING,
                        "Unexpected error while accepting new connection", ex);
            }
        }
    }
}
