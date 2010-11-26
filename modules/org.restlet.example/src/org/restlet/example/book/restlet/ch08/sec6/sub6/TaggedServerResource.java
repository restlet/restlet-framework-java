package org.restlet.example.book.restlet.ch08.sec6.sub6;

import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class TaggedServerResource extends ServerResource {

    @Get
    public Representation represent() {
        Representation result = new StringRepresentation("hello, world");
        // Tagging resource's representation.
        result.setTag(new Tag("helloworld"));
        return result;
    }

    @Put
    public void store(Representation entity) {
        System.out.println("Storing a new entity.");
    }

}
