package org.restlet.example.book.restlet.ch08.sec5.sec5;

import org.restlet.resource.ClientResource;

public class TraceClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8082/?key=value");
        resource.get().write(System.out);
    }
}
