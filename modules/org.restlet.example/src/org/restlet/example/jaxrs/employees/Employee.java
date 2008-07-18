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