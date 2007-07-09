/*
 * Copyright 2005-2007 Noelios Consulting.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class DefaultStreamConnection extends AbstractConnection implements
        StreamConnection {

    private JxtaSocketServer server;

    private ConnectionListener listener;

    /**
     * @return the listener
     */
    public ConnectionListener getListener() {
        return listener;
    }

    /**
     * @return the server
     */
    public JxtaSocketServer getServer() {
        return server;
    }

    public DefaultStreamConnection(URI id, String name,
            NetworkHandler networkHandler) {
        super(id, name, networkHandler);
    }

    // todo: impelement
    public InputStream getInputStream() {
        return null;
    }

    // todo: implement
    public OutputStream getOutputStream() {
        return null;
    }

    // todo: implement
    public void receive(Socket socket) {
    }

    public void start() throws NetworkException {
        if (server != null) {
            return;
        }

        // todo: subgroups
        PeerGroup group = getNetworkHandler().getNetwork().getNetPeerGroup();

        // todo: socket factory based on id
        server = new JxtaSocketServer(getName(), group, PipeID.create(getId()));

        server.start();
    }

    public void stop() throws NetworkException {
        if (server == null) {
            return;
        }

        server.stop();
    }
}