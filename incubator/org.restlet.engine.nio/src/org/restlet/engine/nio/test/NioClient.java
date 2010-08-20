package org.restlet.engine.nio.test;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;

public class NioClient {

    public static void main(String[] args) throws Exception {
        Engine.getInstance().getRegisteredClients()
                .add(0, new org.restlet.engine.nio.HttpClientHelper(null));

        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("tracing", "true");
        client.getContext().getParameters().add("minThreads", "1");
        client.getContext().getParameters().add("lowThreads", "30");
        client.getContext().getParameters().add("maxThreads", "40");
        client.getContext().getParameters().add("maxQueued", "20");
        client.start();

        ClientResource cr = new ClientResource("http://www.restlet.org/");
        cr.setNext(client);
        cr.get().write(System.out);
    }

}
