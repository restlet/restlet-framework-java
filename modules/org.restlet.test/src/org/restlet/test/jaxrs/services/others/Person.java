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