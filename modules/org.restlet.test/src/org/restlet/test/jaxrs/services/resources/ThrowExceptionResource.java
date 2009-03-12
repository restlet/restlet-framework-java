/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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