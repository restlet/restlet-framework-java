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

package com.noelios.restlet.ext.jxta.server;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocketAddress;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

abstract class JxtaServer {

    private String name;
    private PeerGroup group;
    private PipeAdvertisement pipe;
    private boolean publish = true;

    public JxtaServer(String name, PeerGroup group) {
        this(name, group, null);
    }

    public JxtaServer(String name, PeerGroup group, PipeAdvertisement pipe) {
        this.name = name;
        this.group = group;
        this.pipe = pipe;
    }

    public String getName() {
        return name;
    }

    public PeerGroup getPeerGroup() {
        return group;
    }

    public PipeAdvertisement getPipeAdvertisement() {
        return pipe;
    }

    public boolean getPublish() {
        return publish;
    }

    public void setPublish(final boolean publish) {
        this.publish = publish;
    }

    public SocketAddress getSocketAddress() {
        return new JxtaSocketAddress(getPeerGroup(), getPipeAdvertisement());
    }

    public void publish() {
        if (getPublish()) {
            DiscoveryService discovery = getPeerGroup().getDiscoveryService();

            try {
                discovery.publish(getPipeAdvertisement());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            discovery.remotePublish(getPipeAdvertisement(), DiscoveryService.ADV);
        }
    }
}