package org.restlet.example.book.restlet.ch04.sec3.server;

import org.restlet.example.book.restlet.ch04.sec3.common.RootResource;
import org.restlet.resource.ServerResource;

/**
 * Root resource implementation.
 */
public class RootServerResource extends ServerResource implements RootResource {

    public String represent() {
        return "Welcome to the " + getApplication().getName() + " !";
    }

}
