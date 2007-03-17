/*
 * Copyright 2007 Noelios Consulting.
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

package com.noelios.restlet.ext.jxta.server;

import com.noelios.restlet.ext.jxta.net.JxtaMulticastServer;
import com.noelios.restlet.ext.jxta.prototype.Constants;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.ext.network.NetworkListener;
import net.jxta.pipe.PipeID;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class HttpServerHelper extends com.noelios.restlet.http.HttpServerHelper {

    private static Logger logger = Logger.getLogger(HttpServerHelper.class.getName());
    private NetworkHandler network;
    private Server server;

    public HttpServerHelper(org.restlet.Server server) {
        super(server);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "instantiated[server]: [" + server + "]");
        }
    }

    @Override
    public void start() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "http starting");
        }

        startNetwork();
        startServer();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "http started");
        }
    }

    @Override
    public void stop() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "http stopping");
        }

        stopServer();
        stopNetwork();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "http stopped");
        }
    }

    private void startNetwork()
            throws NetworkException {
        if (network != null) {
            throw new IllegalStateException("network already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "starting network");
        }

        network = new NetworkHandler(new NetworkListener() {
            public void notify(NetworkEvent ne) {
                // todo: do better
                System.out.println("NetworkEvent");

                if (ne.getCause() instanceof GroupEvent) {
                    System.out.println("GroupEvent");
                }
            }
        });

        network.start();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "started network");
        }
    }

    private void stopNetwork() {
        if (network == null) {
            throw new IllegalStateException("network not started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopping network");
        }

        network.stop();

        network = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopped network");
        }
    }

    private void startServer() throws NetworkException {
        if (server != null) {
            throw new IllegalStateException("server already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "starting server");
        }

        // todo: consider ServerFactory (see meerkat)
        server = new JxtaMulticastServer(Constants.PROTOTYPE_MULTICAST_PIPE_NAME,
                network.getNetwork().getNetPeerGroup(),
                PipeID.create(Constants.PROTOTYPE_MULTICAST_PIPE_ID));

        server.start();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "started server");
        }
    }

    private void stopServer() throws NetworkException {
        if (server == null) {
            throw new IllegalStateException("server not started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopping server");
        }

        server.stop();

        server = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopped server");
        }
    }
}
