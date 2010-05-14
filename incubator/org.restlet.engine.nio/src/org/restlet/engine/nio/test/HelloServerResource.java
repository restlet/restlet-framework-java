package org.restlet.engine.nio.test;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloServerResource extends ServerResource {

    @Get
    public String represent() {
        return "hello, world!";
    }

}
