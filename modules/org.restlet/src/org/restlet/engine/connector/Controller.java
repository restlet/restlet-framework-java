/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.connector;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * Controls the IO work of parent connector helper.
 * 
 * @author Jerome Louvel
 */
public abstract class Controller {

    /** The parent connector helper. */
    protected final ConnectionHelper<?> helper;

    /** Indicates if the controller is overloaded. */
    protected boolean overloaded;

    /** Indicates if the task is running. */
    protected boolean running;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     */
    public Controller(ConnectionHelper<?> helper) {
        this.helper = helper;
        this.overloaded = false;
        this.running = false;
    }

    /**
     * Initializes the controller before entering the control loop.
     */
    protected void doInit() {
    }

    /**
     * Method called-back with the controller stops running.
     */
    protected void doRelease() {
    }

    /**
     * Do the actual controller work. Called by the {@link #run()} to provide an
     * easy method to overload.
     * 
     * @param sleepTime
     */
    protected void doRun(long sleepTime) throws IOException {
        getHelper().control();
    }

    /**
     * Returns the parent connector helper.
     * 
     * @return The parent connector helper.
     */
    protected ConnectionHelper<?> getHelper() {
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
     * Indicates if the task is running.
     * 
     * @return True if the task is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Listens on the given server socket for incoming connections.
     */
    public void run() {
        try {
            doInit();
            setRunning(true);
            long sleepTime = getHelper().getControllerSleepTimeMs();
            boolean hasWorkerThreads = getHelper().hasWorkerThreads();
            boolean isWorkerServiceOverloaded;

            while (isRunning()) {
                try {
                    if (hasWorkerThreads) {
                        isWorkerServiceOverloaded = getHelper()
                                .isWorkerServiceOverloaded();

                        if (isOverloaded() && !isWorkerServiceOverloaded) {
                            setOverloaded(false);
                            getHelper()
                                    .getLogger()
                                    .info("Connector overload ended. Accepting new work again");
                            getHelper().traceWorkerService();
                        } else if (isWorkerServiceOverloaded) {
                            setOverloaded(true);
                            getHelper()
                                    .getLogger()
                                    .info("Connector overload detected. Stop accepting new work");
                            getHelper().traceWorkerService();
                        }
                    }

                    doRun(sleepTime);
                } catch (Throwable ex) {
                    this.helper.getLogger().log(Level.WARNING,
                            "Unexpected error while controlling connector", ex);
                    setRunning(false);
                }
            }
        } catch (Throwable e) {
            this.helper.getLogger().log(Level.WARNING,
                    "Unexpected error while controlling connector", e);
            setRunning(false);
        } finally {
            doRelease();
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

}
