package org.restlet.example.book.restlet.ch4;

import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.noelios.restlet.StatusFilter;

public class SimpleHttpServerWithFilter {
    public static void main(String[] args) {

        // Creates a Restlet whose response to each request is "Hello, world".
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                // response.setEntity("hello, world", MediaType.TEXT_PLAIN);
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        };

        final StatusFilter statusFilter = new StatusFilter(null, true,
                "admin@example.com", "http://www.example.com");
        statusFilter.setNext(restlet);

        // Instantiates a simple HTTP server that redirects all incoming
        // requests to the router.
        try {
            new Server(Protocol.HTTP, statusFilter).start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
