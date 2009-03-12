/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @Produces(APPLICATION_JSON)
    public JSONObject getJsonObject() throws Exception {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name1", "value1");
        jsonObject.put("name2", "value2");
        return jsonObject;
    }

    @GET
    @Path("person")
    @Produces( { TEXT_XML, APPLICATION_XML, APPLICATION_JSON })
    public Person getPerson(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return new Person(firstname, lastname);
    }

    @GET
    @Path("String")
    @Produces(APPLICATION_JSON)
    public String getString() {
        return "{name:value}";
    }

    @POST
    @Path("JSONObject")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public String post(JSONObject jsonObject) throws Exception {
        return jsonObject.getString("name");
    }
}