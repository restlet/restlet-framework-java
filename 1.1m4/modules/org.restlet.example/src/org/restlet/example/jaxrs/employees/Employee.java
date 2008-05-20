package org.restlet.example.jaxrs.employees;

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

    public String getSex() {
        return this.sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public URI getDepartmentUri() {
        return this.departmentUri;
    }

    public void setDepartmentUri(URI departmentUri) {
        this.departmentUri = departmentUri;
    }
}