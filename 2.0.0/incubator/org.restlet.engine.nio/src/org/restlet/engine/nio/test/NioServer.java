package org.restlet.engine.nio.test;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class NioServer {

    public static void main(String[] args) throws Exception {

        // Start an HTTP server using this NIO connector
        Server server = new Server(Protocol.HTTP, 9999);
        server.setNext(HelloServerResource.class);
        server.start();

    }

}
