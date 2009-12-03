package org.restlet.ext.guice.example;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DefaultResource extends ServerResource {
    @Get
    public String represent() {
        return "Default resource, try /hello/resource";
    }
}
