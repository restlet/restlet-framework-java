/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.bench;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class NioClient {

    public static void main(String[] args) throws Exception {
        // TraceHandler.register();

        ConnectorHelper<Client> helper;
         helper = new org.restlet.ext.nio.HttpClientHelper(null);
        // helper = new org.restlet.ext.httpclient.HttpClientHelper(null);
        //helper = new org.restlet.ext.net.HttpClientHelper(null);
        Engine.getInstance().getRegisteredClients().add(0, helper);
        // [ifdef jse] instruction
        // Engine.setLogLevel(Level.FINE);

        final Client client = new Client(new Context(), Protocol.HTTP);
        // client.getContext().getParameters().add("tracing", "true");
        // client.getContext().getParameters().add("persistingConnections", "false");
        // client.getContext().getParameters().add("minThreads", "1");
        // client.getContext().getParameters().add("lowThreads", "30");
        // client.getContext().getParameters().add("maxThreads", "40");
        // client.getContext().getParameters().add("maxQueued", "20");
        // client.getContext().getParameters().add("directBuffers", "false");
        // client.start();

        String uri = "http://127.0.0.1:7777/";
        int iterations = 100;
        ClientResource cr = new ClientResource(uri);
        cr.setRetryOnError(false);
        cr.setNext(client);
        Representation r = null;

        System.out.println("Calling resource: " + uri + " " + iterations
                + " times");
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            r = cr.get();
            // r.exhaust();
            // System.out.println(r.getText());

            System.out.println("Copying to the local file " + i + "/"
                    + iterations);
            ClientResource fr = new ClientResource("file://C/Test/run" + i
                    + ".pdf");
            fr.put(r);
            System.out.println("Copy done!");
        }

        long total = (System.currentTimeMillis() - start);
        long avg = total / iterations;
        System.out.println("Bench completed in " + total
                + " ms. Average time per call: " + avg + " ms");
    }
}
