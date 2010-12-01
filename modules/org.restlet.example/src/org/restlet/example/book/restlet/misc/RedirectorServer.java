package org.restlet.example.book.restlet.misc;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class RedirectorServer {
    public static void main(String[] args) throws Exception {
        // Create the HTTP server and listen on port 8182
        new Server(Protocol.HTTP, 8182, HelloServerResource.class).start();
        // Create the HTTP server and listen on port 8183
        new Server(Protocol.HTTP, 8183, new RedirectorRestlet()).start();
    }

}
