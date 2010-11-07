package org.restlet.example.book.restlet.misc;

import org.restlet.resource.ClientResource;

public class MultiProtocolsClient {
    public static void main(String[] args) throws Exception {

        ClientResource resource = new ClientResource("file:///c:/boot.ini");
        resource.get().write(System.out);

        resource = new ClientResource("http://localhost:8182");
        resource.get().write(System.out);
    }
}
