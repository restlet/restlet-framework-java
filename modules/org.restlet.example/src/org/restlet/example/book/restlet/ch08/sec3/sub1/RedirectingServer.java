package org.restlet.example.book.restlet.ch08.sec3.sub1;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch01.HelloServerResource;

public class RedirectingServer {

    public static void main(String[] args) throws Exception {
        // Launching the HelloServerResource on port 8111
        new Server(Protocol.HTTP, 8111, HelloServerResource.class).start();

        // Launching the RedirectionResource on port 8113
        new Server(Protocol.HTTP, 8113, OldServerResource.class).start();
    }

}
