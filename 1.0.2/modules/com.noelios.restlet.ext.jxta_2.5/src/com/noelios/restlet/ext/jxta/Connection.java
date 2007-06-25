package com.noelios.restlet.ext.jxta;

import net.jxta.ext.network.NetworkException;

import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public interface Connection extends Component {

    public URI getId();

    public void start() throws NetworkException;

    public void stop() throws NetworkException;
}