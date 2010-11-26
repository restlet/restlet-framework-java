package org.restlet.example.book.restlet.ch08.sec2;

import org.restlet.resource.ClientResource;

public class RedirectorClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8183");
        resource.get().write(System.out);
    }

}
