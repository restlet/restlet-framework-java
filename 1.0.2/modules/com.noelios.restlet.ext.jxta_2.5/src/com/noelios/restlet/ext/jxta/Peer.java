package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.util.NetworkHandler;

import java.util.Collection;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public interface Peer {

    public void addConnections(Connection... connection);

    public Collection<Connection> getConnections();

    public void removeConnections(Connection ... connection);

    public void start();

    public void stop();

    public NetworkHandler getNetworkHandler();
}
