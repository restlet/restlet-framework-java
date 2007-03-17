package com.noelios.restlet.ext.jxta.net.handler;

import com.noelios.restlet.ext.jxta.net.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class MulticastSocketHandler implements Handler {

    private MulticastSocket socket = null;
    private DatagramPacket datagram = null;

    public MulticastSocketHandler(MulticastSocket socket, DatagramPacket datagram) {
        this.socket = socket;
        this.datagram = datagram;
    }

    public void handle() {
        byte[] data = datagram.getData();

        System.out.println("inbound: " + new String(data));

        String response = "pong";
        byte[] responseData = response.getBytes();
        DatagramPacket responseDatagram = new DatagramPacket(responseData, responseData.length);

        if (socket.isConnected() && ! socket.isClosed()) {
            try {
                socket.send(responseDatagram);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
