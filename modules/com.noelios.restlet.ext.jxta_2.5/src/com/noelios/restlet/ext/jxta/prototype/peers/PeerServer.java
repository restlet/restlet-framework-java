package com.noelios.restlet.ext.jxta.prototype.peers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.noelios.restlet.ext.jxta.server.HttpServerHelper;

import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class PeerServer extends PeerBase {

    HttpServerHelper server = new HttpServerHelper(null);

    @Inject
    public PeerServer(@Named("peer.name")String name,
                      @Named("peer.id")String id,
                      @Named("peer.home")String home,
                      @Named("peer.profile")String profile) {
        super(name, URI.create(id), home, profile);
    }

    public void start() {
        super.start();

        server = new HttpServerHelper(null);

        server.setName(getName());
        server.setId(getId());
        server.setNetworkHandler(getNetworkHandler());

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.stop();
    }
}
