package org.restlet.example.book.restlet.misc;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class CachingClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/");
        // Get a representation
        Representation rep = resource.get();
        System.out.println(resource.getStatus());

        // Get a new Representation, if modified
        resource.getConditions().setModifiedSince(
                rep.getModificationDate());
        rep = resource.get();
        System.out.println(resource.getStatus());
    }

}
