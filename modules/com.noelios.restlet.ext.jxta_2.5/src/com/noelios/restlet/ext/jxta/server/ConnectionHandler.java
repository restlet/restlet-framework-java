package com.noelios.restlet.ext.jxta.server;

import com.noelios.restlet.ext.jxta.server.handler.MulticastSocketHandler;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class ConnectionHandler
        implements Runnable {

    private MulticastSocket multicast = null;
    private DatagramPacket datagram = null;

    public ConnectionHandler(MulticastSocket socket, DatagramPacket datagram) {
        this.multicast = socket;
        this.datagram = datagram;
    }

    public void run() {
        Handler handler = new MulticastSocketHandler(multicast, datagram);

        handler.handle();
    }
}
