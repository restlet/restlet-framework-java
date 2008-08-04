/*
 * Copyright 2005-2008 Noelios Technologies.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
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
    private final EmployeeMgr employeeMgr = EmployeeMgr.get();

    @Context
    private UriInfo uriInfo;

    /**
     * Creates the URI for the location of an created employee.
     * 
     * @param staffNo
     *            the number of the created employee
     * @param extension
     *            the file extension to use for content negotiation
     * @return the URI for the location of an created employee.
     */
    private URI createdLocation(int staffNo, String extension) {
        final UriBuilder locBuilder = this.uriInfo.getRequestUriBuilder();
        locBuilder.path("{staffNo}").extension(extension);
        return locBuilder.build(staffNo);
    }

    /** Creates a new employee from XML or JSON */
    @POST
    @Consumes( { "application/xml", "text/xml", "application/json" })
    public Response createEmployee(Employee employee) {
        final int staffNo = this.employeeMgr.createEmployee(employee);
        final String uriExts = this.uriInfo.getConnegExtension();
        final URI location = createdLocation(staffNo, uriExts);
        return Response.created(location).build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response createEmployee(MultivaluedMap<String, String> employeeData) {
        final Employee employee = new Employee();
        employee.setFirstname(employeeData.getFirst("firstname"));
        employee.setLastname(employeeData.getFirst("lastname"));
        employee.setSex(employeeData.getFirst("sex"));
        employee.setDepartment(employeeData.getFirst("department"));
        final int persNo = this.employeeMgr.createEmployee(employee);
        final URI location = createdLocation(persNo, "html");
        return Response.seeOther(location).build();
    }

    @GET
    @Produces( { "application/xml", "text/xml", "application/json" })
    public EmployeeList getEmployees() {
        final EmployeeList employees = this.employeeMgr.getAll();
        // set detail URIs
        final UriBuilder uriBuilder = this.uriInfo.getRequestUriBuilder();
        uriBuilder.path("{staffNo}");
        // LATER uriBuilder.extension(uriInfo.getPathExtension());
        for (final SmallEmployee employee : employees) {
            employee.setDetails(uriBuilder.build(employee.getStaffNo()));
        }
        return employees;
    }

    @GET
    @Produces("text/html")
    public StreamingOutput getListAsHtml() {
        final EmployeeList employees = getEmployees();
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException {
                final PrintStream ps = new PrintStream(output);
                ps.println("<html><head>");
                ps.println("<title>Employees</title>");
                ps.println("</head></body>");
                ps.println("<h2>Employees</h2>");
                ps.println("<ul>");
                for (final SmallEmployee employee : employees) {
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
                ps.print(EmployeesResource.this.uriInfo.getAbsolutePath());
                ps.println("\" method=\"POST\">");
                ps.println("<table><tr>");
                ps.println("<td>firstname:</td>");
                ps
                        .println("<td><input type=\"text\" name=\"firstname\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>lastname:</td>");
                ps
                        .println("<td><input type=\"text\" name=\"lastname\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>sex:</td>");
                ps.println("<td><input type=\"text\" name=\"sex\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td>department:</td>");
                ps
                        .println("<td><input type=\"text\" name=\"department\" /></td>");
                ps.println("</tr><tr>");
                ps.println("<td></td>");
                ps
                        .println("<td><input type=\"submit\" value=\"create employee\" /></td>");
                ps.println("</tr></table>");
                ps.println("</form>");
                ps.println("</body></html>");
            }
        };
    }

    /** Create sub resource for one concrete employee. */
    @Path("{staffNo}")
    public EmployeeResource getSub(@PathParam("staffNo") int staffNo) {
        if (!this.employeeMgr.exists(staffNo)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return new EmployeeResource(staffNo);
    }
}