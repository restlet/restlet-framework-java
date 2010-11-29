package org.restlet.example.book.restlet.ch08.sec6.sub6;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class ConditionalClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8082/");
        Representation rep = resource.get();

        System.out.println("Putting if tag has changed.");
        resource.getConditions().getNoneMatch().add(rep.getTag());
        resource.put(rep);

        System.out.println(resource.getStatus());
    }

}
