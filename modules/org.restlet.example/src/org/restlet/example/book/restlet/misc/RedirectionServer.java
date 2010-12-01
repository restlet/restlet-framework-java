package org.restlet.example.book.restlet.misc;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class RedirectionServer {

    public static void main(String[] args) throws Exception {
        // Launching the HelloServerResource on port 8182
        new Server(Protocol.HTTP, 8182, HelloServerResource.class).start();
        // Launching the RedirectionResource on port 8183
        new Server(Protocol.HTTP, 8183, OldServerResource.class).start();
    }

}
