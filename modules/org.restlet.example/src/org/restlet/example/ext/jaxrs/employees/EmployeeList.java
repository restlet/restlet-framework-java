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

package org.restlet.example.ext.jaxrs.employees;

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
