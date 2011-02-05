/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.connector;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoState;
import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

/**
 * Controls the IO work of parent connector helper and manages its connections.
 * 
 * @author Jerome Louvel
 */
public class ConnectionController extends Controller implements Runnable {

    /** The NIO selector. */
    private final Selector selector;

    /** The list of new selection registrations. */
    private final Queue<SelectionRegistration> newRegistrations;

    /** The list of new selection registrations. */
    private final Queue<SelectionRegistration> updatedRegistrations;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public ConnectionController(ConnectionHelper<?> helper) {
        super(helper);
        this.newRegistrations = new ConcurrentLinkedQueue<SelectionRegistration>();
        this.updatedRegistrations = new ConcurrentLinkedQueue<SelectionRegistration>();
        this.selector = createSelector();
    }

    /**
     * Control each connection for messages to read or write.
     * 
     * @throws IOException
     */
    protected void controlConnections() throws IOException {
        // Close connections or register interest in NIO operations
        for (Connection<?> conn : getHelper().getConnections()) {
            if (conn.getState() == ConnectionState.CLOSED) {
                getHelper().getConnections().remove(conn);
                getHelper().checkin(conn);
            } else if ((conn.getState() == ConnectionState.CLOSING)
                    && conn.isEmpty()) {
                conn.close(false);
            } else if (conn.hasTimedOut()) {
                if (getHelper().getLogger().isLoggable(Level.FINE)) {
                    getHelper().getLogger().fine(
                            "Closing connection with \""
                                    + conn.getSocketAddress()
                                    + "\" due to lack of activity during "
                                    + getHelper().getMaxIoIdleTimeMs() + " ms");
                }

                conn.close(false);
            } else if (conn.updateState()) {
                getUpdatedRegistrations().add(conn.getRegistration());
            } else if (conn.getInboundWay().getIoState() == IoState.READY) {
                conn.getInboundWay().onSelected(
                        conn.getInboundWay().getRegistration());
            } else if (conn.getOutboundWay().getIoState() == IoState.READY) {
                conn.getOutboundWay().onSelected(
                        conn.getOutboundWay().getRegistration());
            }
        }
    }

    /**
     * Creates a new NIO selector.
     * 
     * @return A new NIO selector.
     */
    protected Selector createSelector() {
        Selector result = null;

        try {
            result = Selector.open();
        } catch (IOException ioe) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to open the controller's NIO selector", ioe);
        }

        return result;
    }

    @Override
    protected void doInit() {
        // Done in the controller for thread safety reason regarding the byte
        // buffers part of the pooled connections
        getHelper().createConnectionPool();
    }

    @Override
    protected void doRelease() {
        try {
            getSelector().close();
        } catch (IOException e) {
            getHelper().getLogger().log(Level.WARNING,
                    "Unable to close the NIO selector", e);
        }
    }

    @Override
    protected void doRun(long sleepTime) throws IOException {
        super.doRun(sleepTime);
        registerKeys();
        updateKeys();
        selectKeys(sleepTime);
        controlConnections();
    }

    /**
     * Returns the queue of new selection registrations.
     * 
     * @return The queue of new selection registrations.
     */
    protected Queue<SelectionRegistration> getNewRegistrations() {
        return this.newRegistrations;
    }

    /**
     * Returns the NIO selector.
     * 
     * @return The NIO selector.
     */
    protected Selector getSelector() {
        return selector;
    }

    /**
     * Returns the queue of updated selection registrations.
     * 
     * @return The queue of updated selection registrations.
     */
    protected Queue<SelectionRegistration> getUpdatedRegistrations() {
        return this.updatedRegistrations;
    }

    /**
     * Called back when a ready key has been selected.
     * 
     * @param selectedKey
     *            The selected key selected.
     */
    protected void onSelected(SelectionKey selectedKey)
            throws ClosedByInterruptException {
        // Notify the selected way
        try {
            if (selectedKey.attachment() != null) {
                ((SelectionRegistration) selectedKey.attachment())
                        .onSelected(selectedKey.readyOps());
            }
        } catch (CancelledKeyException cke) {
            getHelper().getLogger().log(Level.FINER,
                    "Problem during NIO selection", cke);
            getNewRegistrations().add(
                    ((SelectionRegistration) selectedKey.attachment()));
        }
    }

    /**
     * Registers a selection listener with the underlying selector for the given
     * operations and returns the registration created.
     * 
     * @param selectableChannel
     *            The NIO selectable channel.
     * @param interestOperations
     *            The initial operations of interest.
     * @param listener
     *            The listener to notify.
     * @return The created registration.
     */
    public SelectionRegistration register(SelectableChannel selectableChannel,
            int interestOperations, SelectionListener listener)
            throws IOException {
        SelectionRegistration result = new SelectionRegistration(
                selectableChannel, interestOperations, listener);
        getNewRegistrations().add(result);
        return result;
    }

    /**
     * Registers all the new selection registration requests.
     */
    protected void registerKeys() {
        SelectionRegistration newRegistration = getNewRegistrations().poll();

        while (newRegistration != null) {
            if (getHelper().getLogger().isLoggable(Level.FINEST)) {
                getHelper().getLogger().log(
                        Level.FINEST,
                        "Registering new NIO interest with selector: "
                                + newRegistration);
            }

            newRegistration.register(getSelector());
            newRegistration = getNewRegistrations().poll();
        }
    }

    /**
     * Selects the keys ready for IO operations.
     * 
     * @param sleepTime
     *            The max sleep time.
     * @throws IOException
     * @throws ClosedByInterruptException
     */
    protected void selectKeys(long sleepTime) throws IOException,
            ClosedByInterruptException {
        // Select the connections ready for NIO operations
        int selectCount = getSelector().select(sleepTime);

        if (selectCount > 0) {
            if (getHelper().getLogger().isLoggable(Level.FINEST)) {
                getHelper().getLogger().log(Level.FINEST,
                        "NIO selected " + selectCount + " key(s) !");
            }

            for (Iterator<SelectionKey> keys = getSelector().selectedKeys()
                    .iterator(); keys.hasNext();) {
                // Retrieve the next selected key
                onSelected(keys.next());
                keys.remove();
            }
        }
    }

    @Override
    public void shutdown() throws IOException {
        super.shutdown();
        getSelector().close();
    }

    /**
     * Updates all the selection registrations for new interest or cancellation.
     * 
     * @throws IOException
     */
    protected void updateKeys() throws IOException {
        SelectionRegistration updatedRegistration = getUpdatedRegistrations()
                .poll();

        while (updatedRegistration != null) {
            if (getHelper().getLogger().isLoggable(Level.FINEST)) {
                getHelper().getLogger().log(
                        Level.FINEST,
                        "Updating NIO interest with selector: "
                                + updatedRegistration);
            }

            updatedRegistration.update();
            updatedRegistration = getUpdatedRegistrations().poll();
        }
    }

    /**
     * Wakes up the controller. By default it wakes up the selector.
     */
    public void wakeup() {
        getSelector().wakeup();
    }

}