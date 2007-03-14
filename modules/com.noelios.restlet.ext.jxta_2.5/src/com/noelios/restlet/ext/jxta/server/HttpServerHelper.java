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

import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import com.noelios.restlet.ext.jxta.server.jxta.JxtaMulticastServer;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.ext.network.NetworkListener;

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
            logger.log(Level.FINE, "starting");
        }

        startNetwork();
        startServer();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "started");
        }
    }

    @Override
    public void stop() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopping");
        }

        stopNetwork();
        stopServer();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopped");
        }
    }

    private void startNetwork()
            throws NetworkException {
        if (network != null) {
            throw new IllegalStateException("network already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "network starting");
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
            logger.log(Level.FINE, "network started");
        }
    }

    private void stopNetwork() {
        if (network == null) {
            throw new IllegalStateException("network not started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "network stopping");
        }

        network.stop();

        network = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "network stopped");
        }
    }

    private void startServer() {
        if (network != null) {
            throw new IllegalStateException("server already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server starting");
        }

        // todo: introduce ServerFactory (see meerkat)
        server = new JxtaMulticastServer("proto", network.getNetwork().getNetPeerGroup());

        server.start();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server started");
        }
    }

    private void stopServer() {
        if (network == null) {
            throw new IllegalStateException("server not started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server stopping");
        }

        server.stop();

        server = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server stopped");
        }
    }
}
