package org.restlet.example.book.restlet.ch08.sec3.sub3;

import org.restlet.Application;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch01.HelloServerResource;

public class RangeServer {

    public static void main(String[] args) throws Exception {
        // Instantiating the Application providing the Range Service
        Application app = new Application();

        // Plug the server resource.
        app.setInboundRoot(HelloServerResource.class);

        // Instantiating the HTTP server and listening on port 8082
        new Server(Protocol.HTTP, 8111, app).start();
    }

}
