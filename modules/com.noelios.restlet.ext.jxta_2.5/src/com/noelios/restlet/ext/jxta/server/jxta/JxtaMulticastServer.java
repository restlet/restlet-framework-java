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

package com.noelios.restlet.ext.jxta.server.jxta;

import com.noelios.restlet.ext.jxta.server.ConnectionHandler;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class JxtaMulticastServer
        extends JxtaServer
        implements Runnable {

    private static final int BLOCK = 65536;
    private static final Logger logger = Logger.getLogger(JxtaMulticastServer.class.getName());
    private MulticastSocket server;
    private Thread listener;

    public JxtaMulticastServer(String name, PeerGroup group) {
        this(name, group, null);
    }

    public JxtaMulticastServer(String name, PeerGroup group, PipeAdvertisement pipe) {
        super(name, group, pipe);

        if (pipe == null ||
                !pipe.getType().equals(PipeService.PropagateType)) {
            throw new IllegalArgumentException("invalid pipe: " + pipe);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "instantiated[name][group][pipe]: [" + name + "][" +
                    group + "][" + pipe + "]");
        }
    }

    public void startServer() throws NetworkException {
        if (server != null) {
            throw new IllegalStateException("server already started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server starting");
        }

        try {
            server = new JxtaMulticastSocket(getPeerGroup(), getPipeAdvertisement());
        } catch (IOException ioe) {
            throw new NetworkException("unable to create socket", ioe);
        }

        listener = new Thread(this, getClass().getName() + ":server-listener");

        listener.setDaemon(true);
        listener.start();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server started");
        }
    }

    public void stopServer() throws NetworkException {
        if (server == null) {
            throw new IllegalStateException("server not started");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server stopping");
        }

        listener.interrupt();
        server.close();

        server = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "server stoppped");
        }
    }

    public void run() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "start listening");
        }

        while (!listener.isInterrupted()) {
            byte[] buf = new byte[BLOCK];
            DatagramPacket data = new DatagramPacket(buf, buf.length);

            try {
                server.receive(data);
            } catch (IOException ioe) {
                listener.interrupt();
            }

            ConnectionHandler connectionHandler = new ConnectionHandler(server, data);
            Thread handler = new Thread(connectionHandler, ConnectionHandler.class.getName() + ":handle-connection");

            handler.setDaemon(true);
            handler.start();
        }

        listener = null;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "stop listening");
        }
    }
}
