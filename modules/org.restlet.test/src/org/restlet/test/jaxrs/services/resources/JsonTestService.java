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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;

import org.json.JSONObject;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.tests.JsonTest;

/**
 * @author Stephan Koops
 * @see JsonTest
 */
@Path("jsonTest")
public class JsonTestService {

    @GET
    @Path("JSONObject")
    @ProduceMime(APPLICATION_JSON)
    public JSONObject getJsonObject() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name1", "value1");
        jsonObject.put("name2", "value2");
        return jsonObject;
    }

    @GET
    @Path("person")
    @ProduceMime({TEXT_XML, APPLICATION_XML, APPLICATION_JSON})
    public Person getPerson(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return new Person(firstname, lastname);
    }

    @GET
    @Path("String")
    @ProduceMime(APPLICATION_JSON)
    public String getString() {
        return "{name:value}";
    }
    
    // TODO is defined what should happens if an resource method staticly throws
    // Exceptions?
    @POST
    @Path("JSONObject")
    @ConsumeMime(APPLICATION_JSON)
    @ProduceMime(TEXT_PLAIN)
    public String post(JSONObject jsonObject) throws Exception {
        return jsonObject.getString("name");
    }
}