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
package org.restlet.example.jaxrs.employees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This List is used to be serialized with JAXB.
 * 
 * @author Stephan Koops
 */
@XmlRootElement(name = "employees")
public class EmployeeList implements Iterable<SmallEmployee> {

    private final List<SmallEmployee> employees = new ArrayList<SmallEmployee>();

    public void add(SmallEmployee employees) {
        this.employees.add(employees);
    }

    @XmlElement(name = "employee")
    public List<SmallEmployee> getEmployees() {
        return this.employees;
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<SmallEmployee> iterator() {
        return this.employees.iterator();
    }
}