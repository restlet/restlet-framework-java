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

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.restlet.Server;

import com.noelios.restlet.http.StreamServerHelper;

/**
 * Base JXTA connector.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class JxtaServerHelper extends StreamServerHelper {

    private NetworkHandler networkHandler;

    private PeerGroup peerGroup;

    private PipeAdvertisement pipeAdvertisement;

    /**
     * Constructor.
     * 
     * @param server
     */
    public JxtaServerHelper(Server server) {
        super(server);
    }

    public String getName() {
        return getParameters().getFirstValue("connectionName", "restlet");
    }

    public NetworkHandler getNetworkHandler() {
        return this.networkHandler;
    }

    public PeerGroup getPeerGroup() {
        return this.peerGroup;
    }

    public PipeAdvertisement getPipeAdvertisement() {
        return this.pipeAdvertisement;
    }

    @Override
    public void start() throws Exception {
        this.networkHandler = new DefaultNetworkHandler();
        getNetworkHandler().start();

        this.peerGroup = getNetworkHandler().getNetwork().getNetPeerGroup();
        this.pipeAdvertisement = PipeUtility.createPipeAdvertisement(getName(),
                PipeService.UnicastType, this.peerGroup, PipeUtility
                        .createPipeID(this.peerGroup));
        super.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        getNetworkHandler().stop();
    }

}
