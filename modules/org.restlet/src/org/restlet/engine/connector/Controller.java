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
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.restlet.Response;
import org.restlet.engine.Engine;

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
     * Control the helper for inbound or outbound messages to handle.
     * 
     * @return Indicates if some concrete activity occurred.
     */
    protected boolean controlHelper() {
        boolean result = false;

        // Control pending inbound messages
        for (int i = 0; i < getHelper().getInboundMessages().size(); i++) {
            handleInbound(getHelper().getInboundMessages().poll());
        }

        // Control pending outbound messages
        for (int i = 0; i < getHelper().getOutboundMessages().size(); i++) {
            handleOutbound(getHelper().getOutboundMessages().poll());
        }

        return result;
    }

    /**
     * Initializes the controller before entering the control loop.
     */
    protected void doInit() {
    }

    /**
     * Do the actual controller work. Called by the {@link #run()} to provide an
     * easy method to overload.
     * 
     * @param sleepTime
     */
    protected void doRun(long sleepTime) throws IOException {
        controlHelper();
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
            if (!isOverloaded() && (getWorkerService() != null)
                    && !getWorkerService().isShutdown() && isRunning()) {
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
     * Handle the given inbound message.
     * 
     * @param response
     *            The message to handle.
     */
    protected abstract void handleInbound(final Response response);

    /**
     * Handle the given inbound message.
     * 
     * @param response
     *            The message to handle.
     * @param synchronous
     *            True if the current thread should be used.
     */
    protected void handleInbound(final Response response, boolean synchronous) {
        if (response != null) {
            if (synchronous || !getHelper().isWorkerThreads()) {
                getHelper().handleInbound(response);
            } else {
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
    }

    /**
     * Handle the given outbound message.
     * 
     * @param response
     *            The message to handle.
     */
    protected abstract void handleOutbound(final Response response);

    /**
     * Handle the given outbound message.
     * 
     * @param response
     *            The message to handle.
     * @param synchronous
     *            True if the current thread should be used.
     */
    protected void handleOutbound(final Response response, boolean synchronous) {
        if (response != null) {
            if (synchronous || !getHelper().isWorkerThreads()) {
                getHelper().handleOutbound(response);
            } else {
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
        doInit();
        setRunning(true);
        long sleepTime = getHelper().getControllerSleepTimeMs();

        while (isRunning()) {
            try {
                if (getHelper().isWorkerThreads()) {
                    if (isOverloaded()
                            && !getHelper().isWorkerServiceOverloaded()) {
                        setOverloaded(false);
                        getHelper()
                                .getLogger()
                                .info("Connector overload ended. Accepting new work again");
                        getHelper().traceWorkerService();
                    } else if (getHelper().isWorkerServiceOverloaded()) {
                        setOverloaded(true);
                        getHelper()
                                .getLogger()
                                .info("Connector overload detected. Stop accepting new work");
                        getHelper().traceWorkerService();
                    }
                }

                doRun(sleepTime);
            } catch (Exception ex) {
                this.helper.getLogger().log(Level.WARNING,
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