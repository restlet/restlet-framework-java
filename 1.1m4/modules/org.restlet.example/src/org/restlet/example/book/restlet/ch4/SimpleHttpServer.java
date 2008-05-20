package org.restlet.example.book.restlet.ch4;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SimpleHttpServer {
    public static void main(String[] args) {

        // Creates a Restlet whose response to each request is "Hello, world".
        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };
        // Instantiates a simple HTTP server that redirects all incoming
        // requests to a restlet.
        try {
            new Server(Protocol.HTTP, restlet).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
