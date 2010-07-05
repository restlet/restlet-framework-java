package org.restlet.example.book.restlet.ch07.sec2.server;

import org.restlet.example.book.restlet.ch03.sect5.sub5.common.RootResource;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Root resource implementation.
 */
public class RootServerResource extends WadlServerResource implements
        RootResource {

    @Override
    protected void doInit() throws ResourceException {
        setAutoDescribing(false);
        setName("Root resource");
        setDescription("The root resource of the mail server application");
    }

    public String represent() {
        return "Welcome to the " + getApplication().getName() + " !";
    }

}
