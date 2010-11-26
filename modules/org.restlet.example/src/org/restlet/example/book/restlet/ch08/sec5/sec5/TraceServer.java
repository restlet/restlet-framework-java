package org.restlet.example.book.restlet.ch08.sec5.sec5;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class TraceServer {
    public static void main(String[] args) throws Exception {
        // Instantiating the HTTP server and listening on port 8182
        new Server(Protocol.HTTP, 8182, TraceServerResource.class).start();

    }
}
