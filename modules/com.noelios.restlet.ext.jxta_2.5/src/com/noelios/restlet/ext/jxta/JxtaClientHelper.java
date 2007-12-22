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

import org.restlet.Client;

import com.noelios.restlet.http.StreamClientHelper;

/**
 * Abstract JXTA-based HTTP server connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class JxtaClientHelper extends StreamClientHelper {

    /** The JXTA network handler. */
    private volatile NetworkHandler networkHandler;

    /** The JXTA peer group. */
    private volatile PeerGroup peerGroup;

    /** The JXTA pipe advertisement. */
    private volatile PipeAdvertisement pipeAdvertisement;

    /**
     * Constructor.
     * 
     * @param client
     *                The parent client connector.
     */
    public JxtaClientHelper(Client client) {
        super(client);
    }

    /**
     * Returns the JXTA connection name. Defaults to "restlet".
     * 
     * @return The JXTA connection name.
     */
    public String getConnectionName() {
        return getParameters().getFirstValue("connectionName", "restlet");
    }

    /**
     * Returns the JXTA network handler.
     * 
     * @return The JXTA network handler.
     */
    public NetworkHandler getNetworkHandler() {
        return this.networkHandler;
    }

    /**
     * Returns the JXTA peer group.
     * 
     * @return The JXTA peer group.
     */
    public PeerGroup getPeerGroup() {
        return this.peerGroup;
    }

    /**
     * Returns the JXTA pipe advertisement.
     * 
     * @return The JXTA pipe advertisement.
     */
    public PipeAdvertisement getPipeAdvertisement() {
        return this.pipeAdvertisement;
    }

    @Override
    public void start() throws Exception {
        // Start the network handler
        this.networkHandler = new NetworkHandler();
        getNetworkHandler().start();

        // Initialize the JXTA context
        this.peerGroup = getNetworkHandler().getNetwork().getNetPeerGroup();
        this.pipeAdvertisement = PipeUtility.createPipeAdvertisement(
                getConnectionName(), PipeService.UnicastType, this.peerGroup,
                PipeUtility.createPipeID(this.peerGroup));

        // Continue standard start
        super.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        getNetworkHandler().stop();
    }

}
