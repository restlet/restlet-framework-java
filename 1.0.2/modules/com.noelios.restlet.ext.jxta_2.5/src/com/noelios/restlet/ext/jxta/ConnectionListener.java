package com.noelios.restlet.ext.jxta;

import java.net.InetAddress;

/**
 * @author james@radarnetworks.com
 */
public interface ConnectionListener {

    public void receiveFrom(byte[] data, InetAddress from);
}