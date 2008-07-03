/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.services.resources;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
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

    @GET
    @ProduceMime( { "application/xml", "text/xml" })
    public PersonList getPersons() {
        PersonList list = new PersonList();
        list.add(new Person("Angela", "Merkel"));
        list.add(new Person("Ehud", "Olmert"));
        list.add(new Person("George U.", "Bush"));
        return list;
    }

    @POST
    @ProduceMime( { "application/xml", "text/xml" })
    public Response addPerson(Person person) {
        int id = createPerson(person);
        URI location = uris.getBaseUriBuilder().path(PersonResource.class)
                .build(String.valueOf(id));
        return Response.created(location).build();
    }

    /**
     * @param person
     * @return
     */
    private int createPerson(Person person) {
        return 5;
    }
}