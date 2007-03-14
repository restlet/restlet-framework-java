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

package com.noelios.restlet.ext.jxta.util;

import net.jxta.exception.ConfiguratorException;
import net.jxta.ext.configuration.AbstractConfigurator;
import net.jxta.ext.configuration.Configurator;
import net.jxta.ext.configuration.Profile;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.Network;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.ext.network.NetworkListener;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class NetworkHandler {

    // todo: config hack for the time being
    private static final URI HOME = URI.create(System.getProperty("JXTA_HOME", System.getProperty("user.dir") +
            File.separator + ".restlet.jxta"));
    private static final String PROFILE_RESOURCE = "/com/noelios/restlet/ext/jxta/resources/adhoc.xml";
    private static final String CONFIG_NAME = "restlet";
    private static final String CONFIG_USER = "usr";
    private static final String CONFIG_PASSWORD = "pwd";
    private static final Logger logger = Logger.getLogger(NetworkHandler.class.getName());

    private Network network = null;
    private NetworkListener listener = null;

    public NetworkHandler() {
        this(null);
    }

    public NetworkHandler(final NetworkListener listener) {
        this.listener = listener;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "instantiated[listener]: [" + listener + "]");
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void start()
            throws NetworkException {
        if (network != null) {
            throw new IllegalStateException("network already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "network starting");
        }

        try {
            network = new Network(new AbstractConfigurator(HOME,
                    Profile.get(getClass().getResource(PROFILE_RESOURCE).toURI())) {
                public PlatformConfig createPlatformConfig(Configurator c)
                        throws ConfiguratorException {
                    c.setName(CONFIG_NAME);
                    c.setSecurity(CONFIG_USER, CONFIG_PASSWORD);

                    return c.getPlatformConfig();
                }
            }, listener != null ? listener : createNetworkListener());

            network.start();
        } catch (URISyntaxException use) {
            throw new NetworkException("invalid uri: " + getClass().getResource(PROFILE_RESOURCE), use);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "network started");
        }
    }

    public void stop() {
        if (network == null) {
            throw new IllegalStateException("network already stopped");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopping");
        }

        network.stop();

        network = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stopped");
        }
    }

    // todo: move to pkg protecgted DefaultNetworkListener class
    private NetworkListener createNetworkListener() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "creating listener");
        }
        
        return new NetworkListener() {
            public void notify(NetworkEvent ne) {
                StringBuffer msg = new StringBuffer();
                PeerGroup pg = ne.getPeerGroup();

                msg.append("NetworkEvent: ").
                        append(pg.getPeerGroupName()).
                        append(" ");

                EventObject cause = ne.getCause();

                if (cause != null) {
                    msg.append(cause.getClass().getName()).
                            append(" ");

                    if (cause instanceof RendezvousEvent) {
                        RendezvousEvent re = (RendezvousEvent)cause;
                        String p = re.getPeer();
                        String pid = re.getPeerID().toString();
                        int t = re.getType();

                        pg = ne.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).
                                append(" ").
                                append(p).
                                append(" ").
                                append(pid).
                                append(" ").
                                append(t);
                    } else if (cause instanceof GroupEvent) {
                        GroupEvent ge = (GroupEvent)cause;
                        int t = ge.getType();

                        pg = ge.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).
                                append(" ").
                                append(t);
                    }
                }

                System.out.println(msg);
            }
        };
    }
}
