package com.noelios.restlet.ext.jxta;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public interface AsynchronousConnection extends Connection {

    public void send(byte[] data) throws IOException;

    public void sendTo(byte[] data, InetAddress to) throws IOException;
}