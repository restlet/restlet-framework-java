package org.restlet.example.book.restlet.ch03.sect5.sub5.common;

import org.restlet.resource.Get;

/**
 * Root resource.
 */
public interface RootResource {

    /**
     * Represents the application root with a welcome message.
     * 
     * @return The root representation.
     */
    @Get
    public String represent();

}
