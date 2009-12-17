/**
 * Copyright 2005-2009 Noelios Technologies.
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

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.restlet.Response;

/**
 * Controls the state of the server helper and its managed connections.
 * 
 * @author Jerome Louvel
 */
public class ControllerTask implements Runnable {

    /** The parent server helper. */
    private final BaseServerHelper helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The target server helper.
     */
    public ControllerTask(BaseServerHelper helper) {
        this.helper = helper;
    }

    protected void execute(Runnable task) {
        if (!isWorkerServiceBusy()) {
            getWorkerService().execute(task);
        }
    }

    /**
     * Returns the parent server helper.
     * 
     * @return The parent server helper.
     */
    protected BaseServerHelper getHelper() {
        return helper;
    }

    protected ExecutorService getWorkerService() {
        return getHelper().getWorkerService();
    }

    protected boolean isWorkerServiceBusy() {
        return getHelper().isWorkerServiceBusy();
    }

    /**
     * Listens on the given server socket for incoming connections.
     */
    public void run() {

        while (true) {
            try {
                if (isWorkerServiceBusy()) {
                    getHelper()
                            .getLogger()
                            .log(
                                    Level.INFO,
                                    "Can't submit additional tasks. Consider increasing the maximum number of threads.");
                } else {
                    // Control each connection for requests to read
                    // or responses to write
                    for (final BaseServerConnection conn : getHelper()
                            .getConnections()) {
                        if (conn.canRead()) {
                            execute(new Runnable() {
                                public void run() {
                                    conn.readRequests();
                                }
                            });
                        }

                        if (conn.canWrite()) {
                            execute(new Runnable() {
                                public void run() {
                                    conn.writeResponses();
                                }
                            });
                        }
                    }

                    // Control if there are some pending requests that could be
                    // processed
                    for (int i = 0; i < getHelper().getPendingRequests().size(); i++) {
                        final ConnectedRequest request = getHelper()
                                .getPendingRequests().poll();

                        if (request != null) {
                            execute(new Runnable() {
                                public void run() {
                                    getHelper().handle(request);
                                }
                            });
                        }
                    }

                    // Control if some pending responses that could be moved to
                    // their respective connection queues
                    for (int i = 0; i < getHelper().getPendingResponses()
                            .size(); i++) {
                        final Response response = getHelper()
                                .getPendingResponses().poll();

                        if (response != null) {
                            execute(new Runnable() {
                                public void run() {
                                    getHelper().handle(response);
                                }
                            });
                        }
                    }
                }

                // Sleep a bit
                Thread.sleep(100);
            } catch (Exception ex) {
                this.helper.getLogger().log(Level.WARNING,
                        "Unexpected error while controlling connections", ex);
            }
        }
    }
}