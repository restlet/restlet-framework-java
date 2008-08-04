/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.others;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.services.resources.OwnProviderTestService;
import org.restlet.test.jaxrs.services.resources.PersonsResource;
import org.restlet.test.jaxrs.services.resources.ProviderTestService;
import org.restlet.test.jaxrs.services.tests.PersonsTest;

/**
 * This class is used as data object in the resource classes
 * {@link OwnProviderTestService} and {@link ProviderTestService}. It can be
 * serialized by the {@link CrazyTypeProvider}.
 * 
 * @author Stephan Koops
 * @see PersonList
 * @see PersonsResource
 * @see PersonsTest
 */
@XmlRootElement
public class Person implements Serializable {

    private static final long serialVersionUID = 7691750693436200351L;

    private String firstname;

    private String lastname;

    public Person() {
    }

    public Person(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    /**
     * @return the firstname
     */
    public String getFirstname() {
        return this.firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return this.lastname;
    }

    /**
     * @param firstname
     *            the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @param lastname
     *            the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return this.firstname + " " + this.lastname;
    }
}