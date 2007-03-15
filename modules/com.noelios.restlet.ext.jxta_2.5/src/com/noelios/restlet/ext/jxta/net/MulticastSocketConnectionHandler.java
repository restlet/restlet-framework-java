package com.noelios.restlet.ext.jxta.net;

import com.noelios.restlet.ext.jxta.net.handler.MulticastSocketHandler;
import com.noelios.restlet.ext.jxta.net.Handler;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
class MulticastSocketConnectionHandler
        implements Runnable {

    private MulticastSocket multicast = null;
    private DatagramPacket datagram = null;

    public MulticastSocketConnectionHandler(MulticastSocket socket, DatagramPacket datagram) {
        this.multicast = socket;
        this.datagram = datagram;
    }

    public void run() {
        Handler handler = new MulticastSocketHandler(multicast, datagram);

        handler.handle();
    }
}