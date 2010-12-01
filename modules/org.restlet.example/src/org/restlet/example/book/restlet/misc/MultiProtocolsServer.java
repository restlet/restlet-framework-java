package org.restlet.example.book.restlet.misc;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class MultiProtocolsServer {
    public static void main(String[] args) throws Exception {
        new Server(Protocol.HTTP, 8182, HelloServerResource.class).start();
    }
}
