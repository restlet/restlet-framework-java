package org.restlet.example.book.restlet.ch06.sec5;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class VerificationServer {
    public static void main(String[] args) throws Exception {
        // Instantiating the HTTP server and listening on port 8082
        new Server(Protocol.HTTP, 8082, VerifiedServerResource.class)
                .start();
    }
}
