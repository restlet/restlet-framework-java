package org.restlet.example.book.restlet.ch01;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Simple "hello, world" server resource.
 */
public class HelloResource extends ServerResource {

    @Get
    public String toString() {
        return "hello, world";
    }
}
