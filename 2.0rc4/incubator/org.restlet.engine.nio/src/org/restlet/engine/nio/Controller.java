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
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Response;

/**
 * Controls the state of the server helper and its managed connections.
 * 
 * @author Jerome Louvel
 */
public class Controller implements Runnable {

    /** The parent connector helper. */
    private final BaseHelper<?> helper;

    /** Indicates if the controller is overloaded. */
    private volatile boolean overloaded;

    /** Indicates if the task is running. */
    private volatile boolean running;

    /** The NIO selector. */
    private volatile Selector selector;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public Controller(BaseHelper<?> helper) {
        this.helper = helper;
        this.overloaded = false;
        this.running = false;

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
            } else if ((conn.getState() == ConnectionState.CLOSING)
                    && conn.isEmpty()) {
                conn.close(false);
            } else if (conn.hasTimedOut()) {
                conn.close(false);
                getHelper().getLogger().log(
                        BaseHelper.DEFAULT_LEVEL,
                        "Closing connection with no IO activity during "
                                + getHelper().getMaxIoIdleTimeMs() + " ms.");
            } else {
                conn.registerInterest(getSelector());
            }
        }
    }

    /**
     * Control the helper for inbound or outbound messages to handle.
     * 
     * @return Indicates if some concrete activity occurred.
     */
    protected boolean controlHelper() {
        boolean result = false;

        // Control if there are some pending requests that could
        // be processed
        for (int i = 0; i < getHelper().getInboundMessages().size(); i++) {
            final Response response = getHelper().getInboundMessages().poll();

            if (response != null) {
                execute(new Runnable() {
                    public void run() {
                        getHelper().handleInbound(response);
                    }

                    @Override
                    public String toString() {
                        return "Handle inbound messages";
                    }
                });
            }
        }

        // Control if some pending responses that could be moved
        // to their respective connection queues
        for (int i = 0; i < getHelper().getOutboundMessages().size(); i++) {
            final Response response = getHelper().getOutboundMessages().poll();

            if (response != null) {
                execute(new Runnable() {
                    public void run() {
                        getHelper().handleOutbound(response);
                    }

                    @Override
                    public String toString() {
                        return "Handle outbound messages";
                    }
                });
            }
        }

        return result;
    }

    /**
     * Executes the next task in a separate thread provided by the worker
     * service, only if the worker service isn't busy.
     * 
     * @param task
     *            The next task to execute.
     */
    protected void execute(Runnable task) {
        try {
            if (!isOverloaded() && !getWorkerService().isShutdown()
                    && isRunning()) {
                getWorkerService().execute(task);
            }
        } catch (Exception e) {
            getHelper().getLogger().log(
                    Level.WARNING,
                    "Unable to execute a "
                            + (getHelper().isClientSide() ? "client-side"
                                    : "server-side") + " controller task", e);
        }
    }

    /**
     * Returns the parent connector helper.
     * 
     * @return The parent connector helper.
     */
    protected BaseHelper<?> getHelper() {
        return helper;
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
     * Returns the helper's worker service.
     * 
     * @return The helper's worker service.
     */
    protected ExecutorService getWorkerService() {
        return getHelper().getWorkerService();
    }

    /**
     * Indicates if the controller is overloaded.
     * 
     * @return True if the controller is overloaded.
     */
    public boolean isOverloaded() {
        return overloaded;
    }

    /**
     * Indicates if the task is running.
     * 
     * @return True if the task is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Indicates if the helper's worker service is fully busy and can't accept
     * more tasks.
     * 
     * @return True if the helper's worker service is fully busy.
     */
    protected boolean isWorkerServiceFull() {
        return getHelper().isWorkerServiceFull();
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
            ((Selectable) key.attachment()).onSelected(key);
        }
    }

    /**
     * Listens on the given server socket for incoming connections.
     */
    public void run() {
        setRunning(true);
        long sleepTime = getHelper().getControllerSleepTimeMs();

        while (isRunning()) {
            try {
                if (isOverloaded()) {
                    if (!isWorkerServiceFull()) {
                        setOverloaded(false);
                        getHelper()
                                .getLogger()
                                .log(Level.INFO,
                                        "Accepting new connections and transactions again.");
                    }
                } else {
                    if (isWorkerServiceFull()) {
                        setOverloaded(true);
                        getHelper()
                                .getLogger()
                                .log(
                                        Level.INFO,
                                        "Stop accepting new connections and transactions. Consider increasing the maximum number of threads.");
                    }

                }

                selectKey(sleepTime);
                controlConnections(isOverloaded());
                controlHelper();
            } catch (Exception ex) {
                this.helper.getLogger().log(Level.INFO,
                        "Unexpected error while controlling connector", ex);
            }
        }
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

    /**
     * Indicates if the controller is overloaded.
     * 
     * @param overloaded
     *            True if the controller is overloaded.
     */
    public void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    /**
     * Indicates if the task is running.
     * 
     * @param running
     *            True if the task is running.
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Abort the controller.
     */
    public void shutdown() throws IOException {
        setRunning(false);
    }

    /**
     * Wakes up the controller. By default it wakes up the selector.
     */
    public void wakeup() {
        getSelector().wakeup();
    }

}