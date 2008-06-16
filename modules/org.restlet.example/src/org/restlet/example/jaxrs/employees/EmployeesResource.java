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

import java.io.*;
import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

/**
 * This root resource class represents the list of all available employees.
 * 
 * @author Stephan Koops
 * @see EmployeeResource
 */
@Path("employees")
public class EmployeesResource {

    /** The EmployeeMgr manages the data storage */
    private EmployeeMgr employeeMgr = EmployeeMgr.get();

    @Context
    private UriInfo uriInfo;

    @GET
    @ProduceMime( { "application/xml", "text/xml", "application/json" })
    public EmployeeList getEmployees() {
        EmployeeList employees = employeeMgr.getAll();
        // set detail URIs
        UriBuilder uriBuilder = uriInfo.getPlatonicRequestUriBuilder();
        uriBuilder.path("{staffNo}");
        uriBuilder.extension(uriInfo.getPathExtension());
        for (SmallEmployee employee : employees)
            employee.setDetails(uriBuilder.build(employee.getStaffNo()));
        return employees;
    }

    @POST
    @ConsumeMime( { "application/xml", "text/xml", "application/json" })
    public Response createEmployee(Employee employee) {
        int staffNo = employeeMgr.createEmployee(employee);
        String uriExts = uriInfo.getPathExtension();
        return createdResponse(Status.CREATED, staffNo, uriExts);
    }

    /**
     * Creates a Response, that the employee is created.
     * 
     * @param returnStatus
     *                a Browser should get status "See other"
     */
    private Response createdResponse(Status returnStatus, int staffNo,
            String extension) {
        UriBuilder locBuilder = uriInfo.getPlatonicRequestUriBuilder();
        locBuilder.path("{staffNo}").extension(extension);
        URI location = locBuilder.build(staffNo);
        return Response.status(returnStatus).location(location).build();
    }

    @POST
    @ConsumeMime("application/x-www-form-urlencoded")
    public Response createEmployee(MultivaluedMap<String, String> employeeData) {
        Employee employee = new Employee();
        employee.setFirstname(employeeData.getFirst("firstname"));
        employee.setLastname(employeeData.getFirst("lastname"));
        employee.setSex(employeeData.getFirst("sex"));
        employee.setDepartment(employeeData.getFirst("department"));
        int persNo = employeeMgr.createEmployee(employee);
        return createdResponse(Status.SEE_OTHER, persNo, "html");
    }

    /** Create sub resource for one concrete employee. */
    @Path("{staffNo}")
    public EmployeeResource getSub(@PathParam("staffNo") int staffNo) {
        if (!employeeMgr.exists(staffNo))
            throw new WebApplicationException(Status.NOT_FOUND);
        return new EmployeeResource(staffNo);
    }

    @GET
    @ProduceMime("text/html")
    public StreamingOutput getListAsHtml() {
        final EmployeeList employees = getEmployees();
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException {
                PrintStream ps = new PrintStream(output);
                ps.println("<html><head>");
                ps.println("<title>Employees</title>");
                ps.println("</head></body>");
                ps.println("<h2>Employees</h2>");
                ps.println("<ul>");
                for (SmallEmployee employee : employees) {
                    ps.print("<li><a href=\"");
                    ps.print(employee.getDetails());
                    ps.print("\">");
                    ps.print(employee.getFirstname());
                    ps.print(" ");
                    ps.print(employee.getLastname());
                    ps.print("</a></li>");
                }
                ps.println("</ul>");
                ps.print("<form action=\"");
                ps.print(uriInfo.getAbsolutePath());
                ps.println("\" method=\"POST\">");
                ps.println("<table><tr>");
                ps.println("<td>firstname:</td>");
                ps.println("<td><input type=\"text\" name=\"firstname\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>lastname:</td>");
                ps.println("<td><input type=\"text\" name=\"lastname\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>sex:</td>");
                ps.println("<td><input type=\"text\" name=\"sex\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>department:</td>");
                ps.println("<td><input type=\"text\" name=\"department\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td></td>");
                ps.println("<td><input type=\"submit\" value=\"create employee\" /></td>");
                ps.println("</tr></table>");
                ps.println("</form>");
                ps.println("</body></html>");
            }
        };
    }
}