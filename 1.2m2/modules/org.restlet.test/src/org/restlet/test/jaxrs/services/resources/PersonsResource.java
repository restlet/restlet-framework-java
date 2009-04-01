/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.jaxrs.services.resources;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.others.PersonList;

/**
 * @author Stephan Koops
 * @ee PersonsTest
 */
@Path("persons")
public class PersonsResource {

    @Context
    UriInfo uris;

    @POST
    @Produces( { "application/xml", "text/xml" })
    public Response addPerson(Person person) {
        final int id = createPerson(person);
        final URI location = this.uris.getBaseUriBuilder().path(
                PersonResource.class).build(String.valueOf(id));
        return Response.created(location).build();
    }

    /**
     * @param person
     * @return
     */
    private int createPerson(Person person) {
        // create person in database 
        person.toString();
        
        return 5;
    }

    @GET
    @Produces( { "application/xml", "text/xml" })
    public PersonList getPersons() {
        final PersonList list = new PersonList();
        list.add(new Person("Angela", "Merkel"));
        list.add(new Person("Ehud", "Olmert"));
        list.add(new Person("George U.", "Bush"));
        return list;
    }
}