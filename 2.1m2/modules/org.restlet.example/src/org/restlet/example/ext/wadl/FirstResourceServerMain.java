package org.restlet.example.ext.wadl;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class FirstResourceServerMain {

    public static void main(String[] args) throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8111.
        component.getServers().add(Protocol.HTTP, 8111);

        component.getDefaultHost().attach("/firstResource",
                new FirstResourceApplication());

        // Start the component.
        component.start();
    }

}
