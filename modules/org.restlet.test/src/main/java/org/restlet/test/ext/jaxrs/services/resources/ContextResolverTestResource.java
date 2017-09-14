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

package org.restlet.test.ext.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.ext.jaxrs.services.others.Person;
import org.restlet.test.ext.jaxrs.services.tests.ContextResolverTest;

/**
 * <b>this test is not used yet !!</b>
 * 
 * @author Stephan Koops
 * @see ContextResolverTest
 */
@Path("workers")
public class ContextResolverTestResource {

    /** @see ContextResolverTest#test1() */
    @GET
    @Produces("text/html")
    public Person getHomeUri() {
        return new Person("Helmut", "Kohl");
    }
}
