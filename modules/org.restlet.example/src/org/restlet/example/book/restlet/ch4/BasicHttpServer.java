package org.restlet.example.book.restlet.ch4;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class BasicHttpServer {
    public static void main(String[] args) {
        // Instantiates and starts a simple HTTP server.
        try {
            new Server(Protocol.HTTP, null).start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
