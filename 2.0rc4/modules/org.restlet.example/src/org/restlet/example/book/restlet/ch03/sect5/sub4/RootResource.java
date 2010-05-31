package org.restlet.example.book.restlet.ch03.sect5.sub4;

import org.restlet.resource.Get;
import org.restlet.resource.Options;

/**
 * Annotated Java interface for the root resource.
 */
public interface RootResource {

    @Get
    public String represent();

    @Options
    public String describe();

}
