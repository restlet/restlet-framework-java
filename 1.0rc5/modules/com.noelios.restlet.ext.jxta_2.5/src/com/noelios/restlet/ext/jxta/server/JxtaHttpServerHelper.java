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

import com.noelios.restlet.http.HttpServerHelper;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkListener;
import org.restlet.Server;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class JxtaHttpServerHelper extends HttpServerHelper {

    private NetworkHandler network;

    public JxtaHttpServerHelper(Server server) {
        super(server);
    }

    @Override
    public void start() throws Exception {
        startNetwork();
        startServer();
    }

    @Override
    public void stop() throws Exception {
        stopNetwork();
        stopServer();
    }

    private void startNetwork() {
        if (network == null || network.getNetwork() == null) {
            network = new NetworkHandler(new NetworkListener() {
                public void notify(NetworkEvent ne) {
                    // todo: do better
                    System.out.println("NetworkEvent");

                    if (ne.getCause() instanceof GroupEvent) {
                        System.out.println("GroupEvent");
                    }
                }
            });
        }

        network.start();
    }

    private void startServer() {
        // todo: impl
    }

    private void stopNetwork() {
        if (network != null) {
            network.stop();

            network = null;
        }
    }

    private void stopServer() {
        // todo: impl
    }
}