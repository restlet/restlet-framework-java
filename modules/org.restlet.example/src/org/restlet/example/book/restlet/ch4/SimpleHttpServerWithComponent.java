package org.restlet.example.book.restlet.ch4;
import java.io.IOException;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SimpleHttpServerWithComponent {
    public static void main(String[] args) {
        // Creates a Restlet whose response to each request is "Hello, world".
        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                Client client = new Client(Protocol.FILE);
                Response resp = client.get(LocalReference
                        .createFileReference("d:\\temp\\doc.txt"));
                System.out.println(resp.getStatus());
                try {
                    resp.getEntity().write(System.out);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };

        // Component declaring only one HTTP server connector.
        Component component = new Component();
        component.getServers().add(Protocol.FILE);
        component.getServers().add(Protocol.HTTP);
        component.getDefaultHost().attach("helloWorld", restlet);
        try {
            component.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
