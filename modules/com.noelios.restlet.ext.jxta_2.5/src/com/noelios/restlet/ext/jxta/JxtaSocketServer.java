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
import net.jxta.socket.JxtaServerSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class JxtaSocketServer extends JxtaServer {

    private ServerSocket server;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JxtaSocketServer(String name, PeerGroup group) {
        this(name, group, PipeUtility.createPipeID(group));
    }

    public JxtaSocketServer(String name, PeerGroup group, PipeID pipe) {
        super(PipeUtility.createPipeAdvertisement(name,
                PipeService.UnicastType, group, pipe), name, group);
    }

    public void start() throws NetworkException {
        if (server != null) {
            return;
        }

        try {
            server = new JxtaServerSocket(getPeerGroup(),
                    getPipeAdvertisement());

            server.setSoTimeout(0);
        } catch (IOException ioe) {
            throw new NetworkException("unable to create socket", ioe);
        }

        executor.submit(new Callable<Object>() {
            public Object call() throws IOException {
                while (!executor.isShutdown()) {
                    handle(server.accept());
                }

                return null;
            }
        });

        // todo: publishing should be optional
        publish();
    }

    public void stop() throws NetworkException {
        if (server == null) {
            return;
        }

        try {
            server.close();
        } catch (IOException ioe) {
            throw new NetworkException("can't close socket", ioe);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }

        server = null;
    }

    private void handle(final Socket socket) throws IOException {
        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            public Object call() throws IOException {
                // todo: handle

                return null;
            }
        });
    }
}