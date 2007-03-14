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

package com.noelios.restlet.ext.jxta.server.jxta;

import net.jxta.discovery.DiscoveryService;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocketAddress;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.noelios.restlet.ext.jxta.server.Server;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

abstract class JxtaServer implements Server {

    private static Logger logger = Logger.getLogger(JxtaServer.class.getName());
    private String name;
    private PeerGroup group;
    private PipeAdvertisement pipe;
    private boolean publish = true;

    public JxtaServer(String name, PeerGroup group) {
        this(name, group, null);
    }

    public JxtaServer(String name, PeerGroup group, PipeAdvertisement pipe) {
        this.name = name;
        this.group = group;
        this.pipe = pipe;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "instantiated[name][group][pipe]: [" + name + "][" +
                    group + "][" + pipe + "]");
        }
    }

    public String getName() {
        return name;
    }

    public PeerGroup getPeerGroup() {
        return group;
    }

    public PipeAdvertisement getPipeAdvertisement() {
        return pipe;
    }

    public boolean getPublish() {
        return publish;
    }

    public void setPublish(final boolean publish) {
        this.publish = publish;
    }

    public void start() {
        try {
            startServer();
            publish();
        } catch (NetworkException ne) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "can't start server", ne);
            }
        }
    }

    public void stop() {
        try {
            stopServer();
        } catch (NetworkException ne) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "cant' stop server", ne);
            }
        }
    }

    public abstract void startServer() throws NetworkException;

    public abstract void stopServer() throws NetworkException;

    public SocketAddress getSocketAddress() {
        return new JxtaSocketAddress(getPeerGroup(), getPipeAdvertisement());
    }

    public void publish() throws NetworkException {
        if (publish) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "publishing");
            }

            DiscoveryService discovery = group.getDiscoveryService();

            try {
                discovery.publish(pipe);
            } catch (IOException ioe) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "unable to publish");
                }

                throw new NetworkException("can't publish locally", ioe);
            }

            discovery.remotePublish(pipe, DiscoveryService.ADV);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "published");
            }
        }
    }
}
