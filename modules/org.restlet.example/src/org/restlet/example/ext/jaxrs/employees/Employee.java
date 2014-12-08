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

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains all data about an employee.
 * 
 * @author Stephan Koops
 */
@XmlRootElement
public class Employee extends AbstractEmployee {

    private String sex;

    private String department;

    private URI departmentUri;

    public String getDepartment() {
        return this.department;
    }

    public URI getDepartmentUri() {
        return this.departmentUri;
    }

    public String getSex() {
        return this.sex;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDepartmentUri(URI departmentUri) {
        this.departmentUri = departmentUri;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
