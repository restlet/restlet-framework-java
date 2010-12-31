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
                "file://C/TEST/contacts.json");

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
