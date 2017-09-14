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
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.ConnectionHelper;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.state.ConnectionState;
import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

/**
 * Controls the IO work of parent connector helper and manages its connections.
 * 
 * @author Jerome Louvel
 */
public class ConnectionController extends Controller implements Runnable,
        WakeupListener {

    /** The list of new selection registrations. */
    private final Queue<SelectionRegistration> newRegistrations;

    /** The NIO selector. */
    private volatile Selector selector;

    /** The list of updated selection registrations. */
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
    }

    /**
     * Controls a given connection for messages to read or write. Close inactive
     * connections, select ready connections or register interest in NIO
     * operations.
     * 
     * @param conn
     *            The connection to control.
     * @throws IOException
     */
    protected void controlConnection(Connection<?> conn) throws IOException {
        if (getHelper().getLogger().isLoggable(Level.FINEST)) {
            getHelper().getLogger().log(Level.FINEST,
                    "Connection status: " + conn);
        }

        if (conn.getState() == ConnectionState.CLOSED) {
            // Detach the connection and collect it
            getHelper().getConnections().remove(conn);
            getHelper().checkin(conn);
        } else if ((conn.getState() == ConnectionState.CLOSING)
                && conn.isEmpty()) {
            conn.close(false);
        } else if (conn.hasTimedOut()) {
            conn.onTimeOut();
        } else if (conn.updateState()) {
            getUpdatedRegistrations().add(conn.getRegistration());
        } else if (conn.isReady()) {
            conn.onSelected(conn.getRegistration());
        }
    }

    /**
     * Controls all helper connections.
     * 
     * @throws IOException
     */
    protected void controlConnections() throws IOException {
        for (Connection<?> connection : getHelper().getConnections()) {
            controlConnection(connection);
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
        this.selector = createSelector();
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
        getHelper().getLogger().log(Level.FINEST, "helper.control()");
        super.doRun(sleepTime);
        getHelper().getLogger().log(Level.FINEST, "controlConnections()");
        controlConnections();
        getHelper().getLogger().log(Level.FINEST, "registerKeys()");
        registerKeys();
        getHelper().getLogger().log(Level.FINEST, "updateKeys()");
        updateKeys();
        getHelper().getLogger().log(Level.FINEST,
                "selectKeys(" + sleepTime + ")");
        selectKeys(sleepTime);
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
     * @throws IOException
     */
    protected void onSelected(SelectionKey selectedKey) throws IOException {
        // Notify the selected way
        try {
            if (getHelper().getLogger().isLoggable(Level.FINEST)) {
                getHelper().getLogger().log(
                        Level.FINEST,
                        "NIO selection detected for key: "
                                + selectedKey.attachment());
            }

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
     * Invoked when one of the connections needs to wake up the controller.
     */
    public void onWokeup(SelectionRegistration selectionRegistration)
            throws IOException {
        wakeup();
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
                selectableChannel, interestOperations, listener, this);
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
        if (getHelper().getLogger().isLoggable(Level.FINER)) {
            getHelper().getLogger().log(
                    Level.FINER,
                    "NIO controller about to sleep " + sleepTime
                            + " ms, selecting among "
                            + getSelector().keys().size() + " keys...\n");
        }

        int selectCount = getSelector().select(sleepTime);

        if (selectCount > 0) {
            if (getHelper().getLogger().isLoggable(Level.FINER)) {
                getHelper().getLogger().log(Level.FINER,
                        "NIO controller selected " + selectCount + " key(s) !");
            }

            for (Iterator<SelectionKey> keys = getSelector().selectedKeys()
                    .iterator(); keys.hasNext();) {
                // Retrieve the next selected key
                onSelected(keys.next());
                keys.remove();
            }
        } else if (getHelper().getLogger().isLoggable(Level.FINER)) {
            getHelper().getLogger().log(Level.FINER,
                    "NIO controlled selected no key");
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        wakeup();
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
            if (getHelper().getLogger().isLoggable(Level.FINER)) {
                getHelper().getLogger().log(
                        Level.FINER,
                        "Updating NIO interest with selector: "
                                + updatedRegistration);
            }

            updatedRegistration.update();
            updatedRegistration = getUpdatedRegistrations().poll();
        }
    }

    /**
     * Wakes up the controller thread if wait for an NIO selection.
     */
    public void wakeup() {
        if (getSelector() != null) {
            getSelector().wakeup();

            if (getHelper().getLogger().isLoggable(Level.FINER)) {
                getHelper().getLogger().log(Level.FINER,
                        "NIO controller woke up");
            }
        }
    }

}
