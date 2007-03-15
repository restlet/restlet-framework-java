package com.noelios.restlet.ext.jxta.net.handler;

import com.noelios.restlet.ext.jxta.net.Handler;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.io.IOException;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class MulticastSocketHandler implements Handler {

    private MulticastSocket multicast = null;
    private DatagramPacket datagram = null;

    public MulticastSocketHandler(MulticastSocket socket, DatagramPacket datagram) {
        this.multicast = socket;
        this.datagram = datagram;
    }

    public void handle() {
        byte[] data = datagram.getData();

        System.out.println("inbound: " + new String(data));

        String response = "pong";
        byte[] responseData = response.getBytes();
        DatagramPacket responseDatagram = new DatagramPacket(responseData, responseData.length);

        try {
            multicast.send(responseDatagram);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}