/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.others.PersonList;
import org.restlet.test.jaxrs.services.resources.PersonResource;
import org.restlet.test.jaxrs.services.resources.PersonsResource;
import org.restlet.test.jaxrs.util.OrderedReadonlySet;

/**
 * @author Stephan Koops
 * @see PersonsResource
 * @see PersonResource
 */
public class PersonsTest extends JaxRsTestCase {
    @Override
    protected Application getAppConfig() {
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

        final Response response2 = get(newLocation, MediaType.TEXT_XML);
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        final JaxbRepresentation<Person> repr = new JaxbRepresentation<Person>(
                response2.getEntity(), Person.class);
        final Person person = repr.getObject();
        assertTrue(person.getFirstname().startsWith("firstname"));
        assertEquals("lastname", person.getLastname());
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