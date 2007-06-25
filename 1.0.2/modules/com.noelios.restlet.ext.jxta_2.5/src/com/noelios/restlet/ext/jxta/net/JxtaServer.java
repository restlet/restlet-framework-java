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

import com.noelios.restlet.ext.jxta.Component;
import net.jxta.discovery.DiscoveryService;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocketAddress;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

abstract class JxtaServer implements Component {

    private PipeAdvertisement pipe;
    private String name;
    private PeerGroup group;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JxtaServer(PipeAdvertisement pipe, String name, PeerGroup group) {
        this.pipe = pipe;
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public URI getId() {
        return URI.create("p2p://" + getPipeAdvertisement().getPipeID() + "/" + getName());
    }

    public PeerGroup getPeerGroup() {
        return group;
    }

    public PipeAdvertisement getPipeAdvertisement() {
        return pipe;
    }

    public SocketAddress getSocketAddress() {
        return new JxtaSocketAddress(getPeerGroup(), getPipeAdvertisement());
    }

    public void publish() throws NetworkException {
        DiscoveryService discovery = group.getDiscoveryService();

        try {
            discovery.publish(pipe);
        } catch (IOException ioe) {
            throw new NetworkException("can't publish locally", ioe);
        }

        discovery.remotePublish(pipe, DiscoveryService.ADV);
    }
}