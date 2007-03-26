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

package com.noelios.restlet.ext.jxta.net;

import com.noelios.restlet.ext.jxta.net.handler.MulticastSocketHandler;
import com.noelios.restlet.ext.jxta.util.PipeUtility;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.socket.JxtaMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class JxtaMulticastServer
        extends JxtaServer {

    private static final int BLOCK = 65536;
    private MulticastSocket server;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public JxtaMulticastServer(String name, PeerGroup group) {
        this(name, group, PipeUtility.createPipeID(group));
    }

    public JxtaMulticastServer(String name, PeerGroup group, PipeID pipe) {
        super(name, group, PipeUtility.createPipeAdvertisement(name, PipeService.PropagateType, group, pipe));
    }

    public void startServer() throws NetworkException {
        if (server != null) {
            throw new IllegalStateException("server already started");
        }

        try {
            server = new JxtaMulticastSocket(getPeerGroup(), getPipeAdvertisement());

            server.setSoTimeout(0);
        } catch (IOException ioe) {
            throw new NetworkException("unable to create socket", ioe);
        }

        executor.submit(new Callable<Object>() {
            public Object call() throws IOException {
                listen();

                return null;
            }
        });
    }

    public void stopServer() throws NetworkException {
        if (server == null) {
            throw new IllegalStateException("server not started");
        }

        server.disconnect();
        server.close();
        executor.shutdown();

        while (! executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }

        server = null;
    }

    private void listen()
            throws IOException {
        while (true) {
            byte[] buf = new byte[BLOCK];
            DatagramPacket data = new DatagramPacket(buf, buf.length);

            System.out.println("listening ...");
            server.receive(data);
            System.out.println("listenend");

            final MulticastSocketHandler handler = new MulticastSocketHandler(server, data);

            Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                public Object call() {
                    handler.handle();

                    return null;
                }
            });
        }
    }
}
