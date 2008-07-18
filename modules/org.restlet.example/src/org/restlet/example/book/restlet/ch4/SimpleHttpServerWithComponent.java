package org.restlet.example.book.restlet.ch4;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SimpleHttpServerWithComponent {
    public static void main(String[] args) {
        // Creates a Restlet whose response to each request is "Hello, world".
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };

        // Component declaring only one HTTP server connector.
        final Component component = new Component();
        component.getServers().add(Protocol.FILE);
        component.getServers().add(Protocol.HTTP);
        component.getDefaultHost().attach("/helloWorld", restlet);
        try {
            component.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
