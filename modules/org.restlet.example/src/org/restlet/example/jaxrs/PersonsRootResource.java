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
package org.restlet.example.jaxrs;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * <p>
 * This resource class handles the persons.
 * </p>
 * <p>
 * No real data store is used; the data reading and creation is all faked.
 * </p>
 * 
 * @author Stephan Koops
 * @see Person
 * @see PersonList
 * @see PersonResource
 * @see ExampleAppConfig
 */
@Path("persons")
public class PersonsRootResource {

    /**
     * <p>
     * Returns the persons as XML document.
     * </p>
     * <p>
     * This class demonstrates a resource method: It is annotated with a HTTP
     * method, but not with a &#64;{@link Path}.
     * </p>
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @ProduceMime( { "application/xml", "text/xml" })
    public PersonList getXmlList() {
        // for a good REST style links to the sub resources should be added.
        List<Person> allPersons = getDataStore().getAllPersons();
        return new PersonList(allPersons);
    }

    /**
     * <p>
     * Returns the persons as HTML page.
     * </p>
     * <p>
     * This class demonstrates a resource method: It is annotated with a HTTP
     * method, but not with a &#64;{@link Path}.
     * </p>
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @ProduceMime("text/html")
    public String getHtmlList(@Context UriInfo uriInfo) {
        Collection<Person> persons = getDataStore().getAllPersons();
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
        html.append("<a href=\"");
        html.append(uriInfo.getAbsolutePathBuilder().path("createNew").build());
        html.append("\">create new</a>");

        // create person form
        URI resourceUri = uriInfo.getAbsolutePath();
        appendCreateForm(html, resourceUri);
        return html.toString();
        // You can use Freemarker, Velocity or other Template engines to
        // create the HTML page.
    }

    /**
     * @param html
     * @param personsUri
     */
    private void appendCreateForm(StringBuilder html, URI personsUri) {
        html.append("<form action=\"" + personsUri + "\" method=\"POST\">");
        html.append("<table border=0><tr>");
        html.append("<td>first name:</td>");
        html.append("<td><input type=\"text\" name=\"firstname\" /></td>");
        html.append("</tr><tr>");
        html.append("<td>last name:</td>");
        html.append("<td><input type=\"text\" name=\"lastname\"  /></td>");
        html.append("</tr><tr>");
        html.append("<td></td>");
        html.append("<td><input type=\"submit\" value=\"create person\"></td>");
        html.append("</tr></table>");
        html.append("</form>");
        html.append("</body></html>");
    }
    
    /**
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @ProduceMime("text/html")
    @Path("createNew")
    public String getCreateForm(@Context UriInfo uriInfo) {
        List<String> parentURIs = uriInfo.getAncestorResourceURIs();
        String segment = parentURIs.get(parentURIs.size()-1);
        URI parentUri = uriInfo.getBaseUriBuilder().path(segment).build();
        StringBuilder html = new StringBuilder();
        appendCreateForm(html, parentUri);
        return html.toString();
    }

    /**
     * Creates a person from an HTML form.
     * 
     * @param form
     *                the form, submitted by the Web Browser
     * @param uriInfo
     *                info about the called URI.
     * @return the Response to return to the client.
     */
    @POST
    @ConsumeMime("application/x-www-form-urlencoded")
    public Response createPerson(MultivaluedMap<String, String> form,
            @Context UriInfo uriInfo) {
        Person person = new Person();
        person.setFirstname(form.getFirst("firstname"));
        person.setLastname(form.getFirst("lastname"));
        String newId = String.valueOf(getDataStore().createPerson(person));
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI location = uriBuilder.path(newId).build();
        return Response.seeOther(location).build();
    }

    /**
     * This sub resource locator creates a sub resource instance for a concrete
     * person.
     * 
     * @param personId
     * @return
     */
    @Path("{personId}")
    public PersonResource onePerson(@PathParam("personId") int personId) {
        if (!getDataStore().existPerson(personId))
            throw new WebApplicationException(404); // person not found
        return new PersonResource(personId);
    }

    /**
     * Creates a person from an XML person.
     * 
     * @param person
     *                the person to create
     * @param uriInfo
     *                info about the called URI.
     * @return the Response to return to the client.
     */
    @POST
    @ConsumeMime( { "application/xml", "text/xml" })
    public Response createPerson(Person person, @Context UriInfo uriInfo) {
        String newId = String.valueOf(getDataStore().createPerson(person));
        URI location = uriInfo.getRequestUriBuilder().path(newId).build();
        return Response.created(location).build();
    }

    /**
     * Returns the example DataStore
     * 
     * @return
     */
    private DataStore getDataStore() {
        return DataStore.getInstance();
    }
}