package org.restlet.engine.nio.test;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;

public class NioServer {

    public static void main(String[] args) throws Exception {

        // Start an HTTP server using this NIO connector
        Server server = new Server(new Context(), Protocol.HTTP, 9999);
        server.getContext().getParameters().add("tracing", "true");
        server.setNext(HelloServerResource.class);
        server.start();

    }

}
