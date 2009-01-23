/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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