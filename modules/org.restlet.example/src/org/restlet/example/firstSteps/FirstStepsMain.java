package org.restlet.example.firstSteps;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class FirstStepsMain {

    public static void main(String[] args) throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8111.
        component.getServers().add(Protocol.HTTP, 8111);

        // Attach the sample application.
        component.getDefaultHost().attach("/firstSteps",
                new FirstStepsApplication());

        // Start the component.
        component.start();
    }
}
