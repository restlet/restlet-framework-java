/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.bench;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.routing.Redirector;

public class NioProxy {

    public static void main(String[] args) throws Exception {
        ConnectorHelper<Server> serverHelper = null;
        serverHelper = new org.restlet.ext.nio.HttpServerHelper(null);
        // serverHelper = new org.restlet.ext.jetty.HttpServerHelper(null);
        // serverHelper = new org.restlet.ext.simple.HttpServerHelper(null);

        ConnectorHelper<Client> clientHelper;
        clientHelper = new org.restlet.ext.nio.HttpClientHelper(null);
        // clientHelper = new org.restlet.ext.httpclient.HttpClientHelper(null);
        // clientHelper= new org.restlet.ext.net.HttpClientHelper(null);

        // Register the selected connectors
        Engine.getInstance().getRegisteredServers().add(0, serverHelper);
        Engine.getInstance().getRegisteredClients().add(0, clientHelper);
        // Engine.setLogLevel(Level.FINEST);

        // Create and start a connector instance
        final Server server = new Server(new Context(), Protocol.HTTP, 7777);
        // server.getContext().getParameters().add("tracing", "false");
        // server.getContext().getParameters().add("minThreads", "1");
        // server.getContext().getParameters().add("lowThreads", "30");
        // server.getContext().getParameters().add("maxThreads", "40");
        // server.getContext().getParameters().add("maxQueued", "0");
        // server.getContext().getParameters().add("directBuffers", "false");
        // server.getContext().getParameters().add("workerThreads", "true");
        // server.getContext().getParameters().add("pooledConnections", "true");
        // server.getContext().getParameters().add("maxIoIdleTimeMs",
        // "3000000");

        final Client client = new Client(new Context(), Protocol.HTTP);
        // client.getContext().getParameters().add("persistingConnections",
        // "false");
        // client.getContext().getParameters().add("tracing", "false");
        // client.getContext().getParameters().add("minThreads", "1");
        // client.getContext().getParameters().add("lowThreads", "30");
        // client.getContext().getParameters().add("maxThreads", "40");
        // client.getContext().getParameters().add("maxQueued", "20");
        // client.getContext().getParameters().add("directBuffers", "false");
        // client.start();

        server.setNext(new Redirector(null, "http://localhost:9999",
                Redirector.MODE_SERVER_OUTBOUND) {
            protected void outboundServerRedirect(Reference targetRef,
                    Request request, Response response) {
                serverRedirect(client, targetRef, request, response);
                if (response.getEntity() != null
                        && !request.getResourceRef().getScheme()
                                .equalsIgnoreCase(targetRef.getScheme())) {
                    // Distinct protocol, this data cannot be exposed.
                    response.getEntity().setLocationRef((Reference) null);
                }
            }

        });
        
        System.out.println("NIO PROXY");
        server.start();

    }
}
