package org.restlet.engine.nio.test;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class NioClient {

    public static void main(String[] args) throws Exception {
        Engine.getInstance().getRegisteredClients()
                .add(0, new org.restlet.engine.nio.HttpClientHelper(null));

        Client client = new Client(new Context(), Protocol.HTTP);
        // client.getContext().getParameters().add("tracing", "true");
        client.getContext().getParameters().add("minThreads", "1");
        client.getContext().getParameters().add("lowThreads", "30");
        client.getContext().getParameters().add("maxThreads", "40");
        client.getContext().getParameters().add("maxQueued", "20");
        client.start();

        String uri = "http://www.restlet.org";
        int iterations = 50;
        ClientResource cr = new ClientResource(uri);
        cr.setNext(client);
        Representation r = null;

        System.out.println("Calling resource: " + uri + " " + iterations
                + " times");
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            r = cr.get();
            r.exhaust();
            // r.write(System.out);
            // System.out.println("");
        }

        long total = (System.currentTimeMillis() - start);
        long avg = total / iterations;
        System.out.println("Done in " + total + ". avg per call: " + avg);
    }

}
