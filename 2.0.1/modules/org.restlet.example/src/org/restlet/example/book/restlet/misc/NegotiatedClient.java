package org.restlet.example.book.restlet.misc;

import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

public class NegotiatedClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/");

        // Add a preference for plain text representations
        System.out.println("Get plain text representation:");
        resource.get(MediaType.TEXT_PLAIN).write(System.out);

        // Add a preference for HTML representations
        System.out.println("\nGet HTML representation:");
        resource.get(MediaType.TEXT_HTML).write(System.out);
    }
}
