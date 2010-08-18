/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.nio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.restlet.Server;

/**
 * Listens on the given socket channel for incoming connections and dispatches
 * them to the given handler pool
 * 
 * @author Jerome Louvel
 */
public class ServerController extends Controller {

    /** The latch to countdown when the socket is ready to accept connections. */
    private final CountDownLatch latch;

    /**
     * Constructor.
     * 
     * @param helper
     *            The target server helper.
     */
    public ServerController(BaseServerHelper helper) {
        super(helper);
        this.latch = new CountDownLatch(1);
    }

    /**
     * Awaits for the controller to be effectively started.
     * 
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        this.latch.await();
    }

    /**
     * Returns the parent server helper.
     * 
     * @return The parent server helper.
     */
    protected BaseServerHelper getHelper() {
        return (BaseServerHelper) super.getHelper();
    }

    @Override
    protected void onSelected(SelectionKey key)
            throws ClosedByInterruptException {
        if (!key.isAcceptable()) {
            super.onSelected(key);
        } else if (!isOverloaded()) {
            try {
                // Accept the new connection
                SocketChannel socketChannel = getHelper()
                        .getServerSocketChannel().accept();

                if (socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    int connectionsCount = getHelper().getConnections().size();

                    if ((getHelper().getMaxTotalConnections() == -1)
                            || (connectionsCount <= getHelper()
                                    .getMaxTotalConnections())) {
                        Connection<Server> connection = getHelper()
                                .createConnection(getHelper(), socketChannel,
                                        getSelector());
                        connection.open();
                        getHelper().getConnections().add(connection);
                        getHelper().getLogger().log(
                                BaseHelper.DEFAULT_LEVEL,
                                "New connection accepted. Total : "
                                        + getHelper().getConnections().size());

                        // Attempt to read immediately
                        connection.onSelected(null);
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

    /**
     * Listens on the given server socket for incoming connections.
     */
    public void run() {
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
        super.run();
    }
}