package org.restlet.example.book.restlet.ch01;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Creates and launches a HTTP server listening on port 8182, and transmitting
 * all calls to the HelloServerResource class.
 */
public class HelloServer {

    public static void main(String[] args) throws Exception {
        Server helloServer = new Server(Protocol.HTTP, 8182,
                HelloResource.class);
        helloServer.start();
    }

}
