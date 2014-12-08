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

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.test.ext.jaxrs.services.others.Person;
import org.restlet.test.ext.jaxrs.services.others.PersonList;
import org.restlet.test.ext.jaxrs.services.resources.PersonResource;
import org.restlet.test.ext.jaxrs.services.resources.PersonsResource;
import org.restlet.test.ext.jaxrs.util.OrderedReadonlySet;

/**
 * @author Stephan Koops
 * @see PersonsResource
 * @see PersonResource
 */
public class PersonsTest extends JaxRsTestCase {
    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return new OrderedReadonlySet<Class<?>>(PersonsResource.class,
                        PersonResource.class);
            }
        };
    }

    /**
     * @throws Exception
     * @see PersonsResource#addPerson(Person)
     * @see PersonResource#get(int)
     */
    public void testCreate() throws Exception {
        if (usesTcp()) {
            return;
        }
        final Person newPerson = new Person("Kurt", "Beck");
        final Response response1 = post(new JaxbRepresentation<Person>(
                newPerson));
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_CREATED, response1.getStatus());
        final Reference newLocation = response1.getLocationRef();

        final Response response2 = get(newLocation,
                MediaType.APPLICATION_JAVASCRIPT, MediaType.TEXT_XML);
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        final JaxbRepresentation<Person> repr = new JaxbRepresentation<Person>(
                response2.getEntity(), Person.class);
        final Person person = repr.getObject();
        assertTrue(person.getFirstname().startsWith("firstname"));
        assertEquals("lastname", person.getLastname());

        final Response response3 = get(newLocation, MediaType.ALL);
        sysOutEntityIfError(response3);
        assertEquals(Status.SUCCESS_OK, response3.getStatus());
        final JaxbRepresentation<Person> repr3 = new JaxbRepresentation<Person>(
                response3.getEntity(), Person.class);
        final Person person3 = repr3.getObject();
        assertTrue(person3.getFirstname().startsWith("firstname"));
        assertEquals("lastname", person3.getLastname());
    }

    public void testGetList() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        final JaxbRepresentation<PersonList> personListRepr = new JaxbRepresentation<PersonList>(
                response.getEntity(), PersonList.class);
        final List<Person> persons = personListRepr.getObject().getPersons();
        assertEquals(3, persons.size());
        assertEquals("Angela", persons.get(0).getFirstname());
        assertEquals("Olmert", persons.get(1).getLastname());
        assertEquals("George U.", persons.get(2).getFirstname());
    }
}
