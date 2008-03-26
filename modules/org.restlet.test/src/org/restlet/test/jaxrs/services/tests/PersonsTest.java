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
package org.restlet.test.jaxrs.services.tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.others.PersonList;
import org.restlet.test.jaxrs.services.resources.PersonResource;
import org.restlet.test.jaxrs.services.resources.PersonsResource;

/**
 * @author Stephan Koops
 * @see PersonsResource
 * @see PersonResource
 */
public class PersonsTest extends JaxRsTestCase {
    @Override
    protected ApplicationConfig getAppConfig() {
        return new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                Set<Class<?>> rrcs = new HashSet<Class<?>>(2);
                rrcs.add(PersonResource.class);
                rrcs.add(PersonsResource.class);
                return rrcs;
            }
        };
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return PersonsResource.class;
    }

    public void testGetList() throws Exception {
        Response response = get();
        sysOutEntityIfError(response);
        JaxbRepresentation<PersonList> personListRepr = new JaxbRepresentation<PersonList>(response.getEntity(), PersonList.class);
        List<Person> persons = personListRepr.getObject().getPersons();
        assertEquals(3, persons.size());
        assertEquals("Angela", persons.get(0).getFirstname());
        assertEquals("Olmert", persons.get(1).getLastname());
        assertEquals("George U.", persons.get(2).getFirstname());
    }

    /**
     * @throws Exception
     * @see PersonsResource#addPerson(Person)
     * @see PersonResource#get(int)
     */
    public void testCreate() throws Exception {
        Person newPerson = new Person("Kurt", "Beck");
        Response response1 = post(new JaxbRepresentation<Person>(newPerson));
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_CREATED, response1.getStatus());
        Reference newLocation = response1.getLocationRef();
        
        Response response2 = get(newLocation, MediaType.TEXT_XML);
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        JaxbRepresentation<Person> repr = new JaxbRepresentation<Person>(response2.getEntity(), Person.class);
        Person person = repr.getObject();
        assertTrue(person.getFirstname().startsWith("firstname"));
        assertEquals("lastname", person.getLastname());
    }
}