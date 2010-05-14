package org.restlet.engine.nio.test;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.nio.HttpServerHelper;

public class NioServer {

    public static void main(String[] args) throws Exception {

        // Register the NIO server
        Engine.getInstance().getRegisteredServers().add(0,
                new HttpServerHelper(null));

        // Start an HTTP server using this NIO connector
        Server server = new Server(Protocol.HTTP, 9999);
        server.setNext(HelloServerResource.class);
        server.start();

    }

}
