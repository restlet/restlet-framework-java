package org.restlet.example.book.restlet.ch4;

import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SimpleHttpServerWithRouter {
    public static void main(String[] args) {

        // Creates a Restlet whose response to each request is "Hello, world".
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };

        final Router router = new Router();
        router.attach("/route01", restlet);

        // Instantiates a simple HTTP server that redirects all incoming
        // requests to the router.
        try {
            new Server(Protocol.HTTP, router).start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
