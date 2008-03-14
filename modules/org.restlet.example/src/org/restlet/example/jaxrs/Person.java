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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is used to demonstrate JAXB serializing.
 * 
 * @author Stephan Koops
 * @see PersonList
 * @see PersonResource
 * @see PersonsRootResource
 */
@XmlRootElement
public class Person {

    private Integer id;

    private String firstname;

    private String lastname;

    /**
     * Creates a person data object.
     */
    public Person() {
    }

    /**
     * Creates a person data object with the given ID.
     * 
     * @param id
     */
    public Person(int id) {
        this.id = id;
    }

    /**
     * Creates a person data object with the given firstname and lastname, without an ID.
     * 
     * @param firstname
     * @param lastname
     */
    public Person(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    /**
     * Creates a person data object with the given firstname and lastname, with the given ID.
     * 
     * @param id
     * @param firstname
     * @param lastname
     */
    public Person(int id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    /**
     * @return the firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @param firstname
     *                the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname
     *                the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return firstname + " " + lastname;
    }

    /**
     * Returns the ID, or null if no ID was given.
     * 
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the person.
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
}