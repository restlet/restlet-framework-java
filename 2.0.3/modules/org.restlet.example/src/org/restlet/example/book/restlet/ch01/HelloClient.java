package org.restlet.example.book.restlet.ch01;

import org.restlet.resource.ClientResource;

/**
 * Creates and launches a HTTP client invoking the server listening on port
 * 8182, and writing the response entity on the console.
 */
public class HelloClient {

    public static void main(String[] args) throws Exception {
        ClientResource helloClientresource = new ClientResource(
                "http://localhost:8182/");
        helloClientresource.get().write(System.out);
    }
}
