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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;

import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.providers.MessageBodyWorkersTestProvider;

/**
 * @author Stephan Koops
 * @see MessageBodyWorkersTest
 * @see MessageBodyWorkersTestProvider
 */
@Path("mbw")
public class MessageBodyWorkersTestResource {

    @GET
    @ProduceMime("text/crazy-person")
    public Response get() {
        Person person = new Person("Angela", "Merkel");
        return Response.ok(person).header("h1", "h1v").build();
    }
}