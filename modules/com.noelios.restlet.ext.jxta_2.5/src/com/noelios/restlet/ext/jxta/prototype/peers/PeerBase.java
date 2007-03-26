package com.noelios.restlet.ext.jxta.prototype.peers;

import com.noelios.restlet.ext.jxta.prototype.Peer;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.ext.network.NetworkListener;

import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

abstract class PeerBase implements Peer {

    private String name;
    private URI id;
    private String home;
    private String profile;
    private NetworkHandler networkHandler;

    public PeerBase(String name, URI id, String home, String profile) {
        this.name = name;
        this.id = id;
        this.home = home;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public URI getId() {
        return id;
    }

    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public void start() {
        // todo: get rid of system properties
        System.setProperty(com.noelios.restlet.ext.jxta.util.Constants.JXTA_HOME, home);
        System.setProperty(com.noelios.restlet.ext.jxta.util.Constants.PROFILE, profile);

        networkHandler = new NetworkHandler(new NetworkListener() {
            public void notify(NetworkEvent ne) {
                // todo: implement
                System.out.println("NetworkEvent: " + ne.getCause());

                if (ne.getCause() instanceof GroupEvent) {
                    GroupEvent ge = (GroupEvent)ne.getCause();

                    System.out.println("GroupEvent: " + ge.getType());
                    System.out.println("GroupEvent: " + ge.getPeerGroup());
                }
            }
        });

        try {
            networkHandler.start();
        } catch (NetworkException ne) {
            ne.printStackTrace();
        }
    }

    public void stop() {
        networkHandler.stop();
    }
}
