/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch02.sec5.sub2;

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
