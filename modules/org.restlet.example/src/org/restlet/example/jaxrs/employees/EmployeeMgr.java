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

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a very small business logic. It helds some employees and fakes
 * a data storage. It started with some employees as default. All changes get
 * lost, when the process is stopped.
 * 
 * @author Stephan Koops
 */
public class EmployeeMgr {

    private static EmployeeMgr singelton = new EmployeeMgr();

    /**
     * Returns the singelton instance of the employee manager.
     */
    public static EmployeeMgr get() {
        return singelton;
    }

    /** contains the employees */
    private final Map<Integer, Employee> employees = new HashMap<Integer, Employee>();
    {
        final Employee employee1 = new Employee();
        employee1.setStaffNo(1234);
        employee1.setFirstname("Lucy");
        employee1.setLastname("Smith");
        employee1.setSex("w");
        employee1.setDepartment("research");
        this.employees.put(employee1.getStaffNo(), employee1);

        final Employee employee2 = new Employee();
        employee2.setStaffNo(3210);
        employee2.setFirstname("Jack");
        employee2.setLastname("Jonson");
        employee2.setSex("m");
        employee2.setDepartment("purchase");
        this.employees.put(employee2.getStaffNo(), employee2);
    }

    /**
     * @return the staffNo of the created employee
     */
    public synchronized int createEmployee(Employee employee) {
        final int staffNo = createNewStaffNo();
        employee.setStaffNo(staffNo);
        this.employees.put(employee.getStaffNo(), employee);
        return staffNo;
    }

    /**
     * Creates and return a staff number to be used for a new employee to
     * create.
     */
    private synchronized int createNewStaffNo() {
        int newStaffNo = 3456;
        for (;;) {
            if (!exists(newStaffNo)) {
                return newStaffNo;
            }
            newStaffNo++;
        }
    }

    /**
     * Creates a {@link SmallEmployee} from the given {@link Employee}.
     */
    private synchronized SmallEmployee createSmall(Employee employee) {
        final SmallEmployee smallEmployee = new SmallEmployee();
        smallEmployee.setStaffNo(employee.getStaffNo());
        smallEmployee.setFirstname(employee.getFirstname());
        smallEmployee.setLastname(employee.getLastname());
        return smallEmployee;
    }

    /**
     * Checks, if an employee with the given staff number exists.
     * 
     * @param staffNo
     *            the number of the employee to check for availability.
     * @return true, if there is an employee with the given staffNo, or false if
     *         not.
     */
    public synchronized boolean exists(int staffNo) {
        return this.employees.get(staffNo) != null;
    }

    /**
     * Returns a list of all available employees.
     * 
     * @return a list of all available employees.
     */
    public synchronized EmployeeList getAll() {
        final EmployeeList employees = new EmployeeList();
        for (final Employee employee : this.employees.values()) {
            employees.add(createSmall(employee));
        }
        return employees;
    }

    /**
     * Returns the employee with the given number, or null, if he/she does not
     * exist.
     */
    public synchronized Employee getFull(int staffNo) {
        return this.employees.get(staffNo);
    }

    /**
     * Returns a little amount of information about the employee with the given
     * staff number.
     * 
     * @param staffNo
     *            the number of the employee to get.
     * @return the employee data.
     */
    public synchronized SmallEmployee getSmall(int staffNo) {
        final Employee employee = getFull(staffNo);
        return createSmall(employee);
    }

    public synchronized void remove(int staffNo) {
        this.employees.remove(staffNo);
    }

    public synchronized void update(int staffNo, Employee employee) {
        employee.setStaffNo(staffNo);
        this.employees.put(staffNo, employee);
    }
}