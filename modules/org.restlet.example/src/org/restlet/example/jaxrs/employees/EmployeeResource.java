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

/**
 * This resource class represents one employee.
 * 
 * @author Stephan Koops
 * @see EmployeesResource
 */
public class EmployeeResource {

    private EmployeeMgr employeeMgr = EmployeeMgr.get();

    private int staffNo;

    EmployeeResource(int persNr) {
        this.staffNo = persNr;
    }

    @GET
    @ProduceMime( { "application/xml", "text/xml", "application/json" })
    public Employee get(@Context UriInfo uriInfo) {
        // load employee with requested id
        Employee employee = employeeMgr.getFull(staffNo);

        // set department uri
        UriBuilder departmentUB = uriInfo.getBaseUriBuilder();
        departmentUB.path("departments", "{depId}");
        departmentUB.extension(uriInfo.getPathExtension());
        String department = employee.getDepartment();
        employee.setDepartmentUri(departmentUB.build(department));

        return employee;
    }

    @PUT
    @ConsumeMime( { "application/xml", "text/xml", "application/json" })
    public void update(Employee employee) {
        employeeMgr.update(staffNo, employee);
    }

    @DELETE
    public Object delete(@Context HttpHeaders httpHeaders,
            @Context UriInfo uriInfo) {
        employeeMgr.remove(staffNo);
        if (httpHeaders.getAcceptableMediaTypes().contains(
                MediaType.TEXT_HTML_TYPE))
            return Response.seeOther(createEmployeesUri(uriInfo));
        return null;
    }

    /**
     * @param uriInfo
     * @return
     * @throws IllegalArgumentException
     * @throws UriBuilderException
     */
    private static URI createEmployeesUri(final UriInfo uriInfo) {
        UriBuilder employeesUri = uriInfo.getBaseUriBuilder();
        employeesUri.path(uriInfo.getAncestorResourceURIs().get(0));
        employeesUri.extension(uriInfo.getPathExtension());
        URI build = employeesUri.build();
        return build;
    }

    @GET
    @ProduceMime("text/html")
    public StreamingOutput getHtml(@Context final UriInfo uriInfo) {
        final Employee employee = get(uriInfo);
        final URI employeesUri = createEmployeesUri(uriInfo);
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException {
                PrintStream ps = new PrintStream(output);
                ps.println("<html><head>");
                ps.println("<title>Employee</title>");
                ps.println("</head></body>");
                ps.println("<h2>Employee</h2>");
                ps.println("<table><tr>");
                ps.println("<td>staff no.</td>");
                ps.println("<td>");
                ps.println(employee.getStaffNo());
                ps.println("</td>");
                ps.println("</tr><tr>");
                ps.println("<td>firstname:</td>");
                ps.println("<td>");
                ps.println(employee.getFirstname());
                ps.println("</td>");
                ps.println("</tr><tr>");
                ps.println("<td>lastname:</td>");
                ps.println("<td>");
                ps.println(employee.getLastname());
                ps.println("</td>");
                ps.println("</tr><tr>");
                ps.println("<td>sex:</td>");
                ps.println("<td>");
                ps.println(employee.getSex());
                ps.println("</td>");
                ps.println("</tr><tr>");
                ps.println("<td>department:</td>");
                ps.println("<td>");
                ps.println(employee.getDepartment());
                ps.println("</td>");
                // departments are not implemented.
                ps.println("</tr></table>");
                ps.println("<hr>");
                ps.print("<form action=\"");
                ps.print(uriInfo.getAbsolutePath());
                ps.println("?method=DELETE\" method=\"POST\">");
                ps.println("<input type=\"submit\" value=\"Delete employee\" />");
                ps.println("</form>");
                ps.print("<hr><a href=\"");
                ps.print(employeesUri);
                ps.print("\">all employees</a>");
                ps.println("</body></html>");
            }
        };
    }
}