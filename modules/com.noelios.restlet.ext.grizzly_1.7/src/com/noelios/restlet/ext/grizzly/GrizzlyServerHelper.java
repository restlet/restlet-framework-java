/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.restlet.Server;

import com.noelios.restlet.http.HttpServerHelper;
import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListener;
import com.sun.grizzly.DefaultPipeline;
import com.sun.grizzly.TCPSelectorHandler;

/**
 * Base Grizzly connector.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class GrizzlyServerHelper extends HttpServerHelper {
    /** The Grizzly controller. */
    private volatile Controller controller;

    /**
     * Constructor.
     * 
     * @param server
     *                The server to help.
     */
    public GrizzlyServerHelper(Server server) {
        super(server);
        this.controller = null;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();

        if (this.controller == null) {
            // Configure a new controller
            this.controller = new Controller();
            configure(this.controller);
        }

        getLogger().info("Starting the Grizzly " + getProtocols() + " server");
        final CountDownLatch latch = new CountDownLatch(1);
        final Controller controller = this.controller;
        new Thread() {
            @Override
            public void run() {
                try {
                    controller.addStateListener(new ControllerStateListener() {

                        public void onException(Throwable arg0) {
                            latch.countDown();
                        }

                        public void onReady() {
                            if (getServer().getPort() == 0) {
                                TCPSelectorHandler tsh = (TCPSelectorHandler) controller
                                        .getSelectorHandler(Controller.Protocol.TCP);
                                setEphemeralPort(tsh.getPortLowLevel());
                            }

                            latch.countDown();
                        }

                        public void onStarted() {
                        }

                        public void onStopped() {
                        }

                    });

                    controller.start();
                } catch (IOException e) {
                    getLogger().log(Level.WARNING,
                            "Error while starting the Grizzly controller", e);
                }
            }
        }.start();

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            latch.await();
        } catch (InterruptedException ex) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    /**
     * Configures the Grizzly controller.
     * 
     * @param controller
     *                The controller to configure.
     */
    protected abstract void configure(Controller controller) throws Exception;

    @Override
    public synchronized void stop() throws Exception {
        super.stop();

        if (this.controller != null) {
            getLogger().info(
                    "Stopping the Grizzly " + getProtocols() + " server");
            this.controller.stop();
            this.controller.getPipeline().stopPipeline();
            this.controller.setPipeline(new DefaultPipeline());
        }
    }
}
