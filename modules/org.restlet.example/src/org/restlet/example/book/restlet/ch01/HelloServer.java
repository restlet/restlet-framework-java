package org.restlet.example.book.restlet.ch01;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Creates and launches a HTTP server listening on port 8082, and transmitting
 * all calls to the HelloServerResource class.
 */
public class HelloServer {

    public static void main(String[] args) throws Exception {
        Server helloServer = new Server(Protocol.HTTP, 8082,
                HelloServerResource.class);
        helloServer.start();
    }

}
