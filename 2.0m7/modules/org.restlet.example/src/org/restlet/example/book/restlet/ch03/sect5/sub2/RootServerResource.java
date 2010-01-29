package org.restlet.example.book.restlet.ch03.sect5.sub2;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Illustrating server resource life cycle.
 */
public class RootServerResource extends ServerResource {

    /**
     * Constructor disabling content negotiation and indicating if the
     * identified resource already exists.
     */
    public RootServerResource() {
        setNegotiated(false);
        // setExisting(false);
    }

    @Override
    protected void doInit() throws ResourceException {
        System.out.println("The root resource was initialized.");
    }

    @Override
    protected void doCatch(Throwable throwable) {
        System.out.println("An exception was thrown in the root resource.");
    }

    @Override
    protected void doRelease() throws ResourceException {
        System.out.println("The root resource was released.\n");
    }

    /**
     * Handle the HTTP GET method by returning a simple textual representation.
     */
    @Override
    protected Representation get() throws ResourceException {
        System.out.println("The GET method of root resource was invoked.");
        return new StringRepresentation("This is the root resource");
    }

    /**
     * Handle the HTTP OPTIONS method by illustrating the impact of throwing an
     * exception.
     */
    @Override
    protected Representation options() throws ResourceException {
        System.out.println("The OPTIONS method of root resource was invoked.");
        throw new RuntimeException("Not yet implemented");
    }
}
