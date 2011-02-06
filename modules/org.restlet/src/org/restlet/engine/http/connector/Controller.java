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

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.restlet.Response;
import org.restlet.engine.Engine;

/**
 * Controls the state of the server helper and its managed connections.
 * 
 * @author Jerome Louvel
 */
public class Controller extends BaseTask {

    /** The parent server helper. */
    private final BaseHelper<?> helper;

    /** Indicates if the controller is overloaded. */
    private volatile boolean overloaded;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public Controller(BaseHelper<?> helper) {
        this.helper = helper;
        this.overloaded = false;
    }

    /**
     * Control each connection for messages to read or write.
     * 
     * @throws IOException
     */
    protected void controlConnections() throws IOException {
        for (final Connection<?> conn : getHelper().getConnections()) {
            if (conn.getState() == ConnectionState.CLOSED) {
                getHelper().getConnections().remove(conn);
            } else if ((conn.getState() == ConnectionState.CLOSING)
                    && !conn.isBusy()) {
                conn.close();
            } else {
                if ((isOverloaded() && !getHelper().isClientSide())
                        || conn.canWrite()) {
                    execute(new Runnable() {
                        public void run() {
                            conn.writeMessages();
                        }

                        @Override
                        public String toString() {
                            return "Write connection messages";
                        }
                    });
                }

                if ((isOverloaded() && getHelper().isClientSide())
                        || conn.canRead()) {
                    execute(new Runnable() {
                        public void run() {
                            conn.readMessages();
                        }

                        @Override
                        public String toString() {
                            return "Read connection messages: "
                                    + conn.canRead();
                        }
                    });
                }
            }
        }
    }

    /**
     * Control the helper for inbound or outbound messages to handle.
     */
    protected void controlHelper() {
        // Control if there are some pending requests that could
        // be processed
        for (int i = 0; i < getHelper().getInboundMessages().size(); i++) {
            final Response response = getHelper().getInboundMessages().poll();

            if (response != null) {
                execute(new Runnable() {
                    public void run() {
                        try {
                            getHelper().handleInbound(response);
                        } finally {
                            Engine.clearThreadLocalVariables();
                        }
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
                        try {
                            getHelper().handleOutbound(response);
                        } finally {
                            Engine.clearThreadLocalVariables();
                        }
                    }

                    @Override
                    public String toString() {
                        return "Handle outbound messages";
                    }
                });
            }
        }
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
     * Indicates if the helper's worker service is fully busy and can't accept
     * more tasks.
     * 
     * @return True if the helper's worker service is fully busy.
     */
    protected boolean isWorkerServiceFull() {
        return getHelper().isWorkerServiceFull();
    }

    /**
     * Listens on the given server socket for incoming connections.
     */
    public void run() {
        setRunning(true);

        while (isRunning() || !getHelper().getConnections().isEmpty()) {
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

                    controlConnections();
                    controlHelper();
                }

                // Sleep a bit
                Thread.sleep(getHelper().getControllerSleepTimeMs());
            } catch (Exception ex) {
                this.helper.getLogger().log(Level.FINE,
                        "Unexpected error while controlling connector", ex);
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

}