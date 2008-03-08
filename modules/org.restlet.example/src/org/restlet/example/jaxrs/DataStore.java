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

/**
 * This data store fakes a database.
 * 
 * @author Stephan Koops
 */
public class DataStore {

    private static DataStore instance = new DataStore();

    /**
     * Returns the instance of the faking DataStore
     * 
     * @return
     */
    public static final DataStore getInstance() {
        return instance;
    }

    /**
     * Fakes the loading of a person with the given id. Returns always the same
     * person.
     * 
     * @param personId
     * @return
     */
    public Person loadPerson(int personId) {
        System.out.println("Load person id " + personId);
        Person person = new Person(personId);
        person.setFirstname("Angela");
        person.setLastname("Merkel");
        return person;
    }

    /**
     * Fakes the removing of the person with the given ID.
     * 
     * @param personId
     */
    public void removePerson(int personId) {
        System.out.println("Person with id " + personId + " removed.");
    }

    /**
     * Fakes the creation of the given person in the database
     * 
     * @param person
     * @return
     */
    public int createPerson(Person person) {
        int newId = 5; // create id.
        System.out.println("The person " + person.getFirstname() + " "
                + person.getLastname() + " would be created with id " + newId);
        return newId;
    }

    /**
     * Returns a list of all Persons (fake: returns 3 persons).
     * 
     * @return
     */
    public List<Person> getAllPersons() {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person(1, "George U.", "Buch"));
        persons.add(new Person(2, "Gordon", "Brown"));
        persons.add(new Person(3, "Angela", "Merkel"));
        return persons;
    }

    /**
     * Checks, if the person exists in the faked database.
     * 
     * @param personId
     * @return
     */
    public boolean existPerson(int personId) {
        return personId > 0 && personId < 10;
    }

    /**
     * Updates the given person.
     * 
     * @param personId 
     * @param person
     */
    public void updatePerson(int personId, Person person) {
        System.out.println("The person with id " + personId + " ("
                + person.getFirstname() + " " + person.getLastname()
                + ") was updated");
    }
}