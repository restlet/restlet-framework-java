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

package org.restlet.test.ext.jaxrs.services.others;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.restlet.test.ext.jaxrs.services.providers.TextCrazyPersonProvider;
import org.restlet.test.ext.jaxrs.services.resources.OwnProviderTestService;
import org.restlet.test.ext.jaxrs.services.resources.PersonsResource;
import org.restlet.test.ext.jaxrs.services.resources.ProviderTestService;
import org.restlet.test.ext.jaxrs.services.tests.PersonsTest;

/**
 * This class is used as data object in the resource classes
 * {@link OwnProviderTestService} and {@link ProviderTestService}. It can be
 * serialized by the {@link TextCrazyPersonProvider}.
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
