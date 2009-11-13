package org.restlet.ext.guice.examples;

import org.restlet.resource.*;

public class DefaultResource extends ServerResource {
    @Get
    public String represent() {
        return "Default resource, try /hello/resource or /hello/handler";
    }
}
