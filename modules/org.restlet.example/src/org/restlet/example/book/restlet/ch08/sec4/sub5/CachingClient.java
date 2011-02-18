package org.restlet.example.book.restlet.ch08.sec4.sub5;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class CachingClient {

    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource("http://localhost:8111/");

        // Get a representation
        Representation rep = resource.get();
        System.out.println("Modified: " + rep.getModificationDate());
        System.out.println("Expires: " + rep.getExpirationDate());
        System.out.println("E-Tag: " + rep.getTag());
    }
}
