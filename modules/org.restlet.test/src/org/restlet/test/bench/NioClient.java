package org.restlet.test.bench;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class NioClient {

    public static void main(String[] args) throws Exception {
        Engine.getInstance()
                .getRegisteredClients()
                .add(0, new org.restlet.engine.connector.HttpClientHelper(null));

        Client client = new Client(new Context(), Protocol.HTTP);
        // client.getContext().getParameters().add("tracing", "true");
        client.getContext().getParameters().add("minThreads", "1");
        client.getContext().getParameters().add("lowThreads", "30");
        client.getContext().getParameters().add("maxThreads", "40");
        client.getContext().getParameters().add("maxQueued", "20");
        // client.getContext().getParameters().add("directBuffers", "false");
        client.start();

        String uri = "http://www.restlet.org/downloads/2.1/restlet-jse-2.1snapshot.zip";
        int iterations = 1;
        ClientResource cr = new ClientResource(uri);
        cr.setNext(client);
        Representation r = null;
        ClientResource fr = new ClientResource(
                "file://C/TEST/restlet-jse-2.1snapshot.zip");

        System.out.println("Calling resource: " + uri + " " + iterations
                + " times");
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            r = cr.get();
            System.out.println("Copying to the local file");
            fr.put(r);
        }

        long total = (System.currentTimeMillis() - start);
        long avg = total / iterations;
        System.out.println("Done in " + total + ". avg per call: " + avg);
    }

}
