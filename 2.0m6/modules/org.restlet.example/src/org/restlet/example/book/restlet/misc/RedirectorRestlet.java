package org.restlet.example.book.restlet.misc;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

public class RedirectorRestlet extends Restlet {

    @Override
    public void handle(Request request, Response response) {
        // Update URI of the incoming request
        request.setResourceRef("http://localhost:8182/");
        // Create a basic HTTP client
        Client client = new Client(Protocol.HTTP);
        // Let the client handle the request and response
        client.handle(request, response);
    }

}
