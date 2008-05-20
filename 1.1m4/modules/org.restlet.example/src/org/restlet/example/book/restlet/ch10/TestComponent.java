package org.restlet.example.book.restlet.ch10;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Creates a component.
 * 
 */
public class TestComponent {

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        // Add a new HTTP server connector
        component.getServers().add(Protocol.HTTP, 8182);
        // Add a new FILE client connector
        component.getClients().add(Protocol.FILE);

        // Attach the application to the component and start it
        component.getDefaultHost().attach("/dynamicApplication",
                new DynamicApplication(component.getContext()));

        component.start();

        // Request the XML file
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.GET,
                "http://localhost:8182/dynamicApplication/transformer");
        request.getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_HTML));
        Response response = client
                .handle(request);
        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            response.getEntity().write(System.out);
        }

        component.stop();
    }

}
