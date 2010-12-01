package org.restlet.example.book.restlet.misc;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloServerResource extends ServerResource {

    @Get
    public String hello() {
        return "hello, world";
    }
}
