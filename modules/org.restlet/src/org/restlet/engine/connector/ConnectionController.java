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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.SelectionListener;

/**
 * Controls the IO work of parent connector helper and manages its connections.
 * 
 * @author Jerome Louvel
 */
public abstract class ConnectionController extends Controller implements
        Runnable {

    /** The NIO selector. */
    private volatile Selector selector;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public ConnectionController(ConnectionHelper<?> helper) {
        super(helper);

        try {
            this.selector = Selector.open();
        } catch (IOException ioe) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to open the controller's NIO selector", ioe);
        }
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
        for (final Connection<?> conn : getHelper().getConnections()) {
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
                conn.registerInterest(getSelector());
            }
        }
    }

    /**
     * Returns the NIO selector.
     * 
     * @return The NIO selector.
     */
    public Selector getSelector() {
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
        if (key.attachment() != null) {
            ((SelectionListener) key.attachment()).onSelected(key);
        }
    }

    @Override
    protected void doRun(long sleepTime) throws IOException {
        super.doRun(sleepTime);
        selectKey(sleepTime);
        controlConnections(isOverloaded());
    }

    /**
     * Selects the key ready for IO operations.
     * 
     * @param sleepTime
     *            The max sleep time.
     * @throws IOException
     * @throws ClosedByInterruptException
     */
    protected void selectKey(long sleepTime) throws IOException,
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
     * Wakes up the controller. By default it wakes up the selector.
     */
    public void wakeup() {
        getSelector().wakeup();
    }

}