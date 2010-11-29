package org.restlet.example.book.restlet.ch08.sec2;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch01.HelloServerResource;

public class RedirectingServer {

    public static void main(String[] args) throws Exception {
        // Launching the HelloServerResource on port 8082
        new Server(Protocol.HTTP, 8082, HelloServerResource.class).start();

        // Launching the RedirectionResource on port 8183
        new Server(Protocol.HTTP, 8183, OldServerResource.class).start();
    }

}
