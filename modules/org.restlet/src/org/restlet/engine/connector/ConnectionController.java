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

package org.restlet.engine.connector;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

/**
 * Controls the IO work of parent connector helper and manages its connections.
 * 
 * @author Jerome Louvel
 */
public abstract class ConnectionController extends Controller implements
        Runnable {

    /** The NIO selector. */
    private final Selector selector;

    /** The list of new selection registrations. */
    private final Queue<SelectionRegistration> newRegistrations;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public ConnectionController(ConnectionHelper<?> helper) {
        super(helper);
        this.newRegistrations = new ConcurrentLinkedQueue<SelectionRegistration>();
        this.selector = createSelector();
    }

    /**
     * Control each connection for messages to read or write.
     * 
     * @param overloaded
     *            Indicates if the controller is overloaded.
     * @throws IOException
     */
    protected void controlConnections(boolean overloaded) throws IOException {
        // Close connections or register interest in NIO operations
        for (Connection<?> conn : getHelper().getConnections()) {
            if (conn.getState() == ConnectionState.CLOSED) {
                getHelper().getConnections().remove(conn);
                getHelper().checkin(conn);
            } else if ((conn.getState() == ConnectionState.CLOSING)
                    && conn.isEmpty()) {
                conn.close(false);
            } else if (conn.hasTimedOut()) {
                conn.close(false);
                getHelper().getLogger().fine(
                        "Closing connection with no IO activity during "
                                + getHelper().getMaxIoIdleTimeMs() + " ms.");
            } else {
                conn.updateState();
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
    protected void doRun(long sleepTime) throws IOException {
        super.doRun(sleepTime);
        updateKeys();
        registerKeys();
        selectKeys(sleepTime);
        controlConnections(isOverloaded());
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
     * Callback when a key has been selected.
     * 
     * @param key
     *            The selected key.
     */
    protected void onSelected(SelectionKey key)
            throws ClosedByInterruptException {
        // Notify the selected way
        System.out.println("onSelected: " + key.interestOps() + " | "
                + key.readyOps() + " | " + key.attachment());

        if (key.attachment() != null) {
            ((SelectionRegistration) key.attachment()).onSelected(key
                    .readyOps());
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
     * 
     * @throws IOException
     */
    protected void registerKeys() throws IOException {
        SelectionRegistration newRegistration = getNewRegistrations().poll();

        while (newRegistration != null) {
            newRegistration.getSelectableChannel().register(getSelector(),
                    newRegistration.getInterestOperations(), newRegistration);
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
        if (getSelector().select(sleepTime) > 0) {
            for (Iterator<SelectionKey> selectedKeys = getSelector()
                    .selectedKeys().iterator(); selectedKeys.hasNext();) {
                // Retrieve the next selected key
                onSelected(selectedKeys.next());

                // Remove the processed key from the set
                selectedKeys.remove();
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
        SelectionRegistration registration = null;

        for (SelectionKey key : getSelector().keys()) {
            if (key.attachment() instanceof SelectionRegistration) {
                registration = (SelectionRegistration) key.attachment();

                if (registration.isCanceled()) {
                    key.cancel();
                } else if (key.isValid()) {
                    key.interestOps(registration.getInterestOperations());
                }
            }
        }
    }

    /**
     * Wakes up the controller. By default it wakes up the selector.
     */
    public void wakeup() {
        getSelector().wakeup();
    }

}