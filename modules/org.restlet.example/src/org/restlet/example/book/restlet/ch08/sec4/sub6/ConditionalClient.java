package org.restlet.example.book.restlet.ch08.sec4.sub6;

import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class ConditionalClient {

    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource("http://localhost:8111/");

        // Get a representation
        Representation rep = resource.get();
        System.out.println(resource.getStatus());

        // Get an updated representation, if modified
        resource.getConditions().setModifiedSince(rep.getModificationDate());
        rep = resource.get();
        System.out.println(resource.getStatus());

        // Get an updated representation, if tag changed
        resource.getConditions().setModifiedSince(null);
        resource.getConditions().getNoneMatch().add(new Tag("xyz123"));
        rep = resource.get();
        System.out.println(resource.getStatus());

        // Put a new representation if tag has not changed
        resource.getConditions().getNoneMatch().clear();
        resource.getConditions().getMatch().add(rep.getTag());
        resource.put(rep);
        System.out.println(resource.getStatus());

        // Put a new representation when a different tag
        resource.getConditions().getMatch().clear();
        resource.getConditions().getMatch().add(new Tag("abcd7890"));
        resource.put(rep);
        System.out.println(resource.getStatus());
    }
}
