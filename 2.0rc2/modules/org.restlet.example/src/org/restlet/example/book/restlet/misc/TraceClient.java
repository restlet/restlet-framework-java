package org.restlet.example.book.restlet.misc;

import org.restlet.resource.ClientResource;

public class TraceClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/?key=value");
        resource.get().write(System.out);
    }
}
