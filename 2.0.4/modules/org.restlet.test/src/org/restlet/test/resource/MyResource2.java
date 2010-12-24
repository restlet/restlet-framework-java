package org.restlet.test.resource;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MyResource2 extends ServerResource {

    @Get
    public Representation represent() {
        return new StringRepresentation("<content/>", MediaType.TEXT_XML);
    }

}
