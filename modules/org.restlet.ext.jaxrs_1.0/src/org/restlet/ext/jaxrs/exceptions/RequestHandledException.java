package org.restlet.ext.jaxrs.exceptions;

/**
 * A RequestHandledException is thrown when an this request is already handled,
 * for example because of an handled exception resulting in an error while
 * method invocation. The Exception or whatever was handled and the necessary
 * data in {@link org.restlet.data.Response} were set, so that the JaxRsRouter
 * must not do anything. <br/> This Exception only indicates this.
 * 
 * @author Stephan Koops
 */
public class RequestHandledException extends Exception {
    private static final long serialVersionUID = 2765454873472711005L;
}