package org.restlet.ext.jaxrs;

import org.restlet.Restlet;

/**
 * This exception is thrown, when the algorithm "Matching Requests to
 * Resource Methods" in Section 3.7.2 of JSR-311-Spec could not find a method.
 * 
 * @author Stephan Koops
 */
class CouldNotFindMethodException extends Exception {
    
    private static final long serialVersionUID = -8436314060905405146L;

    Restlet errorRestlet;

    CouldNotFindMethodException(Restlet errorRestlet, String message) {
        super(message);
        this.errorRestlet = errorRestlet;
    }
}