/*
 * Copyright 2007 Noelios Consulting.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jxta;

import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.socket.JxtaMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class JxtaMulticastServer extends JxtaServer implements AsynchronousConnection {

    private static final int BLOCK = 65536;
    private ConnectionListener listener;
    private MulticastSocket socket;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JxtaMulticastServer(PipeID pipe, String name, PeerGroup group, ConnectionListener listener) {
        super(PipeUtility.createPipeAdvertisement(name, PipeService.PropagateType, group, pipe), name, group);

        this.listener = listener;
    }

    public void send(byte[] data) throws IOException {
        sendTo(data, null);
    }

    public void sendTo(byte[] data, InetAddress to) throws IOException {
        if (socket == null) {
            throw new IllegalStateException("socket is not started");
        }

        DatagramPacket datagram = new DatagramPacket(data, data.length);

        if (to != null) {
            datagram.setAddress(to);
        }

        socket.send(datagram);
    }

    public void start() throws NetworkException {
        if (socket != null) {
            return;
        }

        try {
            socket = new JxtaMulticastSocket(getPeerGroup(), getPipeAdvertisement());

            socket.setSoTimeout(0);
        } catch (IOException ioe) {
            throw new NetworkException("unable to create socket", ioe);
        }

        executor.submit(new Callable<Object>() {
            public Object call() throws IOException {
                while (!executor.isShutdown()) {
                    byte[] buf = new byte[BLOCK];
                    DatagramPacket data = new DatagramPacket(buf, buf.length);

                    System.out.println("listening: ");
                    socket.receive(data);
                    handle(data);
                }

                return null;
            }
        });

        // todo: publishing should be optional
        publish();
    }

    public void stop() throws NetworkException {
        if (socket == null) {
            return;
        }

        socket.disconnect();
        socket.close();
        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }

        socket = null;
    }

    private void handle(final DatagramPacket data) throws IOException {
        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            public Object call() throws IOException {
                if (listener != null) {
                    listener.receiveFrom(data.getData(), data.getAddress());
                }

                return null;
            }
        });
    }
}