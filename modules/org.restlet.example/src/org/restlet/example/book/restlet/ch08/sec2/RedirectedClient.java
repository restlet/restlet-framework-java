package org.restlet.example.book.restlet.ch08.sec2;

import org.restlet.resource.ClientResource;

public class RedirectedClient {

    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource("http://localhost:8113/");
        resource.get().write(System.out);
    }

}
