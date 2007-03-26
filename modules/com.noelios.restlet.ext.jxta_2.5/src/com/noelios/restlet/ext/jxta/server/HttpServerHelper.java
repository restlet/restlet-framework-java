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

import com.noelios.restlet.ext.jxta.net.JxtaMulticastServer;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.NetworkException;
import net.jxta.pipe.PipeID;

import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class HttpServerHelper extends com.noelios.restlet.http.HttpServerHelper {

    private String name;
    private URI id;
    private NetworkHandler networkHandler;
    private Server server;

    public HttpServerHelper(org.restlet.Server server) {
        super(server);
    }

    // todo: move to config
    public void setName(String name) {
        this.name = name;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public void setNetworkHandler(NetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    @Override
    public void start() throws NetworkException {
        server = new JxtaMulticastServer(name, networkHandler.getNetwork().getNetPeerGroup(),
                PipeID.create(id));

        server.start();
    }

    @Override
    public void stop() throws NetworkException {
        server.stop();

        server = null;
    }
}
