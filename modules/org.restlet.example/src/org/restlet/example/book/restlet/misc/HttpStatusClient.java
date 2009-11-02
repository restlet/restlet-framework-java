package org.restlet.example.book.restlet.misc;

import org.restlet.data.Status;
import org.restlet.resource.ClientResource;

public class HttpStatusClient {
    public static void main(String[] args) throws Exception {
        // Defines the resource
        ClientResource resource = new ClientResource(
                "http://www.manning.com");

        // Get the representation of the resource
        resource.get();

        // Prints the status
        Status status = resource.getStatus();
        if (status.isSuccess()) {
            System.out.println("Request successful.");
        } else if (status.isError()) {
            System.out.println("Request failed.");
        }
    }
}
