package com.noelios.restlet.ext.jxta.server;

import net.jxta.ext.network.NetworkException;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public interface Server {

    public void start() throws NetworkException;

    public void stop() throws NetworkException;
}
