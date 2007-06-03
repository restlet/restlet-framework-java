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

package com.noelios.restlet.ext.jxta;

import net.jxta.exception.ConfiguratorException;
import net.jxta.ext.configuration.AbstractConfigurator;
import net.jxta.ext.configuration.Configurator;
import net.jxta.ext.configuration.Profile;
import net.jxta.ext.network.Network;
import net.jxta.ext.network.NetworkException;
import net.jxta.ext.network.NetworkListener;
import net.jxta.impl.protocol.PlatformConfig;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class NetworkHandler {

    // todo: configuration hack
    private static final URI HOME = new File(System.getProperty(
            Constants.JXTA_HOME, System.getProperty("user.dir")
                    + File.separator + ".jxta")).toURI();

    private static final String PROFILE_RESOURCE = System.getProperty(
            Constants.PROFILE,
            "/com/noelios/restlet/ext/jxta/resources/adhoc.xml");

    private static final String CONFIG_NAME = "restlet";

    private static final String CONFIG_USER = "usr";

    private static final String CONFIG_PASSWORD = "pwd";

    private Network network = null;

    private NetworkListener listener = null;

    public NetworkHandler(NetworkListener listener) {
        this.listener = listener;
    }

    public Network getNetwork() {
        return network;
    }

    public void start() throws NetworkException {
        if (network != null) {
            return;
        }

        try {
            network = new Network(new AbstractConfigurator(HOME, Profile
                    .get(getClass().getResource(PROFILE_RESOURCE).toURI())) {
                public PlatformConfig createPlatformConfig(Configurator c)
                        throws ConfiguratorException {
                    c.setName(CONFIG_NAME);
                    c.setSecurity(CONFIG_USER, CONFIG_PASSWORD);

                    return c.getPlatformConfig();
                }
            }, listener);

            network.start();
        } catch (URISyntaxException use) {
            throw new NetworkException("invalid uri: "
                    + getClass().getResource(PROFILE_RESOURCE), use);
        }

        // todo: fix, profile.adhoc !-> PlatformConfig.RdvConfig.mode
        // while (! network.isConnected()) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException ie) {
        // // ignore
        // }
        // }
    }

    public void stop() {
        if (network == null) {
            return;
        }

        network.stop();

        network = null;
    }
}