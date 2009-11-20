package org.restlet.example.book.restlet.ch03.sect5.sub4;

import org.restlet.resource.ServerResource;

/**
 * Implementing the Java annotated resource interface.
 */
public class RootServerResource 
    extends ServerResource implements RootResource {

    public String represent() {
        return "This is the root resource";
    }

    public String describe() {
        throw new RuntimeException("Not yet implemented");
    }

}
