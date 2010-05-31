package org.restlet.example.book.restlet.misc;

import org.restlet.resource.ClientResource;

public class DynamicContentClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/");
        resource.get().write(System.out);
    }

}
