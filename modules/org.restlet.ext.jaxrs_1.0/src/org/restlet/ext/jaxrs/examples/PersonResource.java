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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;

/**
 * This resource class handles a concrete Person.
 * 
 * @author Stephan Koops
 * @see PersonsRootResource
 */
public class PersonResource {

    private int personId;

    /**
     * Creates a new PersonResource.
     * 
     * @see PersonsRootResource#onePerson(int)
     * @param personId
     */
    public PersonResource(int personId) {
        this.personId = personId;
    }

    /**
     * Returns the Person as XML resource.
     * 
     * @return the Person as XML resource.
     */
    @GET
    @ProduceMime( { "application/xml", "text/xml" })
    public Person getXml() {
        return dbLoadPerson();
    }

    /**
     * Returns the Person as HTML page.
     * 
     * @return the Person as HTML page.
     */
    @GET
    @ProduceMime("text/html")
    public String getHtml() {
        Person person = dbLoadPerson();
        StringBuilder html = new StringBuilder();
        html.append("<html><head>\n</head><body>\n");
        html.append("<h1>Person</h1>");
        html.append("firstname: " + person.getFirstname() + "<br/>\n");
        html.append("lastname:  " + person.getLastname() + " <br/>\n");
        html.append("(ID: " + person.getId() + ")\n");
        html.append("</body></html>");
        return html.toString();
        // You can use Freemarker, Velocity or other Template engines here to
        // create the HTML page.
    }

    /**
     * Removes the person with the ID {@link #personId}.
     */
    @DELETE
    public void delete() {
        dbRemovePerson();
    }

    /**
     * Loads the person with the id in this resource object, see
     * {@link #personId}.
     * 
     * @return
     */
    private Person dbLoadPerson() {
        System.out.println("Load person id " + personId);
        Person person = new Person(personId);
        person.setFirstname("Angela");
        person.setFirstname("Merkel");
        return person;
    }

    private void dbRemovePerson() {
        System.out.println("Person with id " + personId + " removed.");
    }
}