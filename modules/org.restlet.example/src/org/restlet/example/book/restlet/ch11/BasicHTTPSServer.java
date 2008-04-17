package org.restlet.example.book.restlet.ch11;

import java.io.File;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class BasicHTTPSServer {
    public static void main(String[] args) {
        // Creates a Restlet whose response to each request is "Hello, world".
        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };

        File keystoreFile = new File("d:\\temp\\certificats", "myKeystore");

        // Component declaring only one HTTPS server connector.
        Component component = new Component();
        component.getServers().add(Protocol.HTTPS, 8182);
        component.getDefaultHost().attach("/helloWorld", restlet);

        // Update component's context with keystore parameters.
        component.getContext().getParameters().add("keystorePath",
                keystoreFile.toURI().toASCIIString());
        component.getContext().getParameters().add("keystorePassword",
                "storepass");
        component.getContext().getParameters().add("keyPassword", "keypass");

        try {
            component.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
