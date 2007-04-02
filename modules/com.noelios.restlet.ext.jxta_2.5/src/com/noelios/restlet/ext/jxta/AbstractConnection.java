package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.util.NetworkHandler;

import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public abstract class AbstractConnection implements Connection {

    private URI id;
    private String name;
    private NetworkHandler networkHandler;

    public AbstractConnection(URI id, String name, NetworkHandler networkHandler) {
        this.id = id;
        this.name = name;
        this.networkHandler = networkHandler;
    }

    public URI getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    protected NetworkHandler getNetworkHandler() {
        return networkHandler;
    }
}