/*
 * Copyright 2005-2007 Noelios Consulting.
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

import net.jxta.ext.network.NetworkException;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class DefaultPeer extends ConnectionManager implements Peer {

    private String home;

    private String profile;

    private NetworkHandler networkHandler;

    public DefaultPeer(String home, String profile) {
        this.home = home;
        this.profile = profile;

        // todo: get rid of system properties
        System.setProperty(Constants.JXTA_HOME, home);
        System.setProperty(Constants.PROFILE, profile);

        networkHandler = new DefaultNetworkHandler();
    }

    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public void start() {
        try {
            networkHandler.start();
        } catch (NetworkException ne) {
            ne.printStackTrace();
        }

        for (Connection connection : getConnections()) {
            try {
                connection.start();
            } catch (NetworkException e) {
                e.printStackTrace(); // To change body of catch statement use
                // File | Settings | File Templates.
            }
        }
    }

    public void stop() {
        for (Connection connection : getConnections()) {
            try {
                connection.stop();
            } catch (NetworkException e) {
                e.printStackTrace(); // To change body of catch statement use
                // File | Settings | File Templates.
            }
        }

        networkHandler.stop();
    }

    /**
     * @return the home
     */
    public String getHome() {
        return home;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }
}