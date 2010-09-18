package org.restlet.engine.nio.test;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.Engine;

public class NioServer {

    public static void main(String[] args) throws Exception {
        ConnectorHelper<Server> helper = null;
        // helper = new org.restlet.ext.jetty.HttpServerHelper(null);
        // helper = new org.restlet.ext.grizzly.HttpServerHelper(null);
        // helper = new org.restlet.ext.netty.HttpServerHelper(null);
        helper = new org.restlet.ext.simple.HttpServerHelper(null);
        // helper = new org.restlet.engine.http.connector.HttpServerHelper(null);
        // helper = new org.restlet.engine.nio.HttpServerHelper(null);

        // Register the selected connector
        Engine.getInstance().getRegisteredServers().add(0, helper);

        // Create and start a connector instance
        Server server = new Server(new Context(), Protocol.HTTP, 9999);
        // server.getContext().getParameters().add("tracing", "true");
        server.getContext().getParameters().add("minThreads", "1");
        server.getContext().getParameters().add("lowThreads", "30");
        server.getContext().getParameters().add("maxThreads", "40");
        server.getContext().getParameters().add("maxQueued", "0");
        server.getContext().getParameters().add("workerThreads", "true");
        server.getContext().getParameters().add("pooledConnections", "true");
        // server.setNext(HelloServerResource.class);
        server.setNext(new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world!", MediaType.TEXT_PLAIN);
            }
        });
        server.start();

    }
}
