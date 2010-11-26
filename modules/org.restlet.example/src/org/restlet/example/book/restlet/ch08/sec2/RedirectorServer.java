package org.restlet.example.book.restlet.ch08.sec2;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch01.HelloServerResource;

public class RedirectorServer {
    public static void main(String[] args) throws Exception {
        // Create the HTTP server and listen on port 8182
        new Server(Protocol.HTTP, 8182, HelloServerResource.class).start();
        // Create the HTTP server and listen on port 8183
        new Server(Protocol.HTTP, 8183, new RedirectorRestlet()).start();
    }

}
