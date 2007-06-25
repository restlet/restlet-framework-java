package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.util.Constants;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void stop() {
        for (Connection connection : getConnections()) {
            try {
                connection.stop();
            } catch (NetworkException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        networkHandler.stop();
    }
}