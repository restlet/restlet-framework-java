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
package org.restlet.test.jaxrs.services.resources;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.restlet.test.jaxrs.services.tests.ThrowExceptionTest;

/**
 * @author Stephan Koops
 * @see ThrowExceptionTest
 */
@Path("throwExc")
public class ThrowExceptionResource {

    public static final int WEB_APP_EXC_STATUS = 583;

    @GET
    @Path("IOException")
    public String getIoe() throws IOException {
        throw new IOException("This exception is planned for testing !");
    }

    @GET
    @Path("sqlExc")
    public Object getSqlExc() throws SQLException {
        throw new SQLException();
    }

    @GET
    @Path("WebAppExc")
    public String getWebAppExc() throws WebApplicationException {
        throw new WebApplicationException(WEB_APP_EXC_STATUS);
    }

    @GET
    @Path("WebAppExcNullStatus")
    public String getWebAppExcNullStatus() throws WebApplicationException {
        throw new WebApplicationException((Response) null);
    }
}