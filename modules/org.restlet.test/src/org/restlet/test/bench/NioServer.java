/**
 * Copyright 2005-2012 Restlet S.A.S.
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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;

public class NioServer {

    public static void main(String[] args) throws Exception {
        ConnectorHelper<Server> helper = null;
        // helper = new org.restlet.ext.jetty.HttpServerHelper(null);
        // helper = new org.restlet.ext.simple.HttpServerHelper(null);
        helper = new org.restlet.engine.connector.HttpServerHelper(null);

        // Register the selected connector
        Engine.getInstance().getRegisteredServers().add(0, helper);
        // Engine.setLogLevel(Level.FINEST);

        // Create and start a connector instance
        Server server = new Server(new Context(), Protocol.HTTP, 9999);
        server.getContext().getParameters().add("tracing", "false");
        server.getContext().getParameters().add("minThreads", "1");
        server.getContext().getParameters().add("lowThreads", "30");
        server.getContext().getParameters().add("maxThreads", "40");
        server.getContext().getParameters().add("maxQueued", "0");
        server.getContext().getParameters().add("directBuffers", "false");
        server.getContext().getParameters().add("workerThreads", "true");
        server.getContext().getParameters().add("pooledConnections", "true");
        server.getContext().getParameters().add("maxIoIdleTimeMs", "3000000");

        final ClientResource fr = new ClientResource(
                "file://C/TEST/restlet-jse-2.0.5-ff.zip");

        // server.setNext(HelloServerResource.class);
        server.setNext(new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                fr.put(request.getEntity());

                // try {
                // response.setEntity(new InputRepresentation(
                // new FileInputStream("C://TEST/contacts2.txt")));
                // } catch (FileNotFoundException e) {
                // e.printStackTrace();
                // }

                response.setEntity("hello, world!", MediaType.TEXT_PLAIN);
            }
        });

        server.start();

    }
}
