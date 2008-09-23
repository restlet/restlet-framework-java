/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.restlet.Server;

import com.noelios.restlet.http.HttpServerHelper;
import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListener;
import com.sun.grizzly.TCPSelectorHandler;

/**
 * Base Grizzly connector.
 * 
 * @author Jerome Louvel
 */
public abstract class GrizzlyServerHelper extends HttpServerHelper {
    /** The Grizzly controller. */
    private volatile Controller controller;

    /** The Grizzly TCP selector handler. */
    private volatile TCPSelectorHandler selectorHandler;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public GrizzlyServerHelper(Server server) {
        super(server);
        this.controller = null;
    }

    /**
     * Returns the Grizzly TCP selector handler.
     * 
     * @return The Grizzly TCP selector handler.
     */
    public TCPSelectorHandler getSelectorHandler() {
        // Lazy initialization with double-check.
        TCPSelectorHandler s = this.selectorHandler;
        if (s == null) {
            synchronized (this) {
                s = this.selectorHandler;
                if (s == null) {
                    this.selectorHandler = s = new TCPSelectorHandler();
                }
            }
        }
        return s;
    }

    /**
     * Configures the Grizzly controller.
     * 
     * @param controller
     *            The controller to configure.
     */
    protected abstract void configure(Controller controller) throws Exception;

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void start() throws Exception {
        super.start();

        if (this.controller == null) {
            this.controller = new Controller();

            // We should make this handler configurable via parameters
            this.controller.setSelectorHandler(getSelectorHandler());

            // Configure a new controller
            configure(this.controller);
        }

        getLogger().info("Starting the Grizzly " + getProtocols() + " server");
        final CountDownLatch latch = new CountDownLatch(1);
        final Controller controller = this.controller;
        final TCPSelectorHandler selectorHandler = getSelectorHandler();
        new Thread() {
            @Override
            public void run() {
                try {
                    controller.addStateListener(new ControllerStateListener() {

                        public void onException(Throwable arg0) {
                            latch.countDown();
                        }

                        public void onReady() {
                            if (getHelped().getPort() == 0) {
                                setEphemeralPort(selectorHandler
                                        .getPortLowLevel());
                            }

                            latch.countDown();
                        }

                        public void onStarted() {
                        }

                        public void onStopped() {
                        }

                    });

                    controller.start();
                } catch (final IOException e) {
                    getLogger().log(Level.WARNING,
                            "Error while starting the Grizzly controller", e);
                }
            }
        }.start();

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();

        if (this.controller != null) {
            getLogger().info(
                    "Stopping the Grizzly " + getProtocols() + " server");
            this.controller.stop();
        }
    }
}
