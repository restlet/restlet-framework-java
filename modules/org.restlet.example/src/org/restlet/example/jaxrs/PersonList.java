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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This list is serializable by JAXB.
 * 
 * @author Stephan Koops
 * @see Person
 * @see PersonResource
 * @see PersonsRootResource
 */
@XmlRootElement
public class PersonList {

    private List<Person> persons = new ArrayList<Person>();

    /**
     * Creates a new PersonList
     */
    public PersonList() {
    }

    /**
     * Creates a new PersonList with the given persons.
     * @param persons
     */
    public PersonList(List<Person> persons) {
        this.persons = persons;
    }

    /**
     * @return
     */
    @XmlElement(name = "persons")
    public List<Person> getPersons() {
        return persons;
    }

    /**
     * @param persons
     */
    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    /**
     * @param person
     */
    public void add(Person person) {
        this.persons.add(person);
    }
}