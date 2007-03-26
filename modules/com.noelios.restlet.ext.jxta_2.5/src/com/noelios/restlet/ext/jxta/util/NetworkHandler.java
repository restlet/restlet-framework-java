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
import net.jxta.ext.network.*;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class NetworkHandler {

    // todo: config hack for the time being
    private static final URI HOME = URI.create(System.getProperty(Constants.JXTA_HOME,
            System.getProperty("user.dir") + File.separator + ".jxta"));
    private static final String PROFILE_RESOURCE = System.getProperty(Constants.PROFILE,
            "/com/noelios/restlet/ext/jxta/resources/adhoc.xml");
    private static final String CONFIG_NAME = "restlet";
    private static final String CONFIG_USER = "usr";
    private static final String CONFIG_PASSWORD = "pwd";

    private Network network = null;
    private NetworkListener listener = null;

    public NetworkHandler() {
        this(null);
    }

    public NetworkHandler(final NetworkListener listener) {
        this.listener = listener;
    }

    public Network getNetwork() {
        return network;
    }

    public void start()
            throws NetworkException {
        if (network != null) {
            throw new IllegalStateException("network already started");
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
    }

    public void stop() {
        if (network == null) {
            throw new IllegalStateException("network already stopped");
        }

        network.stop();

        network = null;
    }

    // todo: move to pkg protecgted DefaultNetworkListener class
    private NetworkListener createNetworkListener() {
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
