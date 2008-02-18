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
package org.restlet.ext.jaxrs.examples;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Form;

/**
 * <p>
 * This resource class handles the persons.
 * </p>
 * <p>
 * No data store is used; the data reading and creation is all faked.
 * </p>
 * 
 * @author Stephan Koops
 */
@Path("persons")
public class PersonsRootResource {

    /**
     * This sub resource locator creates a sub resource locator for a concrete
     * person.
     * 
     * @param personId
     * @return
     */
    @Path("{personId}")
    public PersonResource onePerson(@PathParam("personId")
    int personId) {
        return new PersonResource(personId);
    }

    /**
     * Returns the persons as XML document.
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @ProduceMime( { "application/xml", "text/xml" })
    public Collection<Person> getXmlList() {
        // for a good REST style links to the sub resources should be added.
        return dbGetAllPersons();
    }

    /**
     * Returns the persons as HTML page
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @ProduceMime("text/html")
    public String getHtmlList(@Context
    UriInfo uriInfo) {
        Collection<Person> persons = dbGetAllPersons();
        StringBuilder html = new StringBuilder();
        html.append("<html><head></head><body>\n");
        
        // persons list
        html.append("<h1>Persons</h1>\n");
        html.append("<ul>\n");
        for (Person person : persons) {
            UriBuilder location = uriInfo.getAbsolutePathBuilder();
            location.path(String.valueOf(person.getId()));
            html.append("<li>");
            html.append("<a href=\"" + location.build() + "\">");
            html.append(person.getFirstname());
            html.append(" ");
            html.append(person.getLastname());
            html.append("</a>");
            html.append("</li>\n");
        }
        html.append("</ul>\n");
        
        // create person form
        URI resourceUri = uriInfo.getAbsolutePath();
        html.append("<form action=\""+resourceUri+"\" method=\"POST\">");
        html.append("<input type=\"text\" name=\"firstname\" /><br/>");
        html.append("<input type=\"text\" name=\"lastname\"  /><br/>");
        html.append("<input type=\"submit\" /><br/>");
        html.append("</form>");
        html.append("</body></html>");
        return html.toString();
        // You can use Freemarker, Velocity or other Template engines here to
        // create the HTML page.
    }

    /**
     * Creates a person from an HTML form.
     * 
     * @param restletForm
     * @param uriInfo
     * 
     * @return the Response
     */
    @POST
    @ConsumeMime("application/x-www-form-urlencoded")
    public Response createPerson(Form restletForm, @Context
    UriInfo uriInfo) {
        Person person = new Person();
        person.setFirstname(restletForm.getFirstValue("firstname"));
        person.setLastname(restletForm.getFirstValue("lastname"));
        String newId = String.valueOf(dbCreatePerson(person));
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI location = uriBuilder.path(newId).build();
        return Response.seeOther(location).build();
    }

    /**
     * Creates a person from an XML person.
     * 
     * @param person
     * @param uriInfo
     * 
     * @return the Response
     */
    @POST
    @ConsumeMime( { "application/xml", "text/xml" })
    public Response createPerson(Person person, @Context
    UriInfo uriInfo) {
        String newId = String.valueOf(dbCreatePerson(person));
        URI location = uriInfo.getRequestUriBuilder().path(newId).build();
        return Response.created(location).build();
    }

    private int dbCreatePerson(Person person) {
        int newId = 1234;
        System.out.println("The person " + person.getFirstname() + " "
                + person.getLastname() + " would be created with id " + newId);
        return newId;
    }

    private Collection<Person> dbGetAllPersons() {
        Collection<Person> persons = new ArrayList<Person>();
        persons.add(new Person(1, "George U.", "Buch"));
        persons.add(new Person(2, "Gordon", "Brown"));
        persons.add(new Person(3, "Angela", "Merkel"));
        return persons;
    }
}