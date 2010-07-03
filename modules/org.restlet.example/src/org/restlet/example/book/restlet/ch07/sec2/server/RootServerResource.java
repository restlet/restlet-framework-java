package org.restlet.example.book.restlet.ch07.sec2.server;

import org.restlet.example.book.restlet.ch03.sect5.sub5.common.RootResource;
import org.restlet.resource.ServerResource;

/**
 * Root resource implementation.
 */
public class RootServerResource extends ServerResource implements RootResource {

    public String represent() {
        return "Welcome to the " + getApplication().getName() + " !";
    }

}
