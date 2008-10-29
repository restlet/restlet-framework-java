/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.providers;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * 
 */
@Provider
public class ThreadLocalContextTestExcMapper implements
        ExceptionMapper<SQLException> {

    @Context
    HttpHeaders httpHeaders;

    private volatile boolean marker = false;

    private final Object counterSync = new Object();

    /**
     * @return
     */
    private MediaType getAccMediaType() {
        final List<MediaType> accMediaTypes = this.httpHeaders
                .getAcceptableMediaTypes();
        if (accMediaTypes.contains(MediaType.TEXT_HTML_TYPE)) {
            return MediaType.TEXT_HTML_TYPE;
        } else {
            return MediaType.TEXT_PLAIN_TYPE;
        }
    }

    /**
     * @param msg
     */
    private void sysout(String msg) {
        System.out.println("ThreadLocalContextTestExcMapper("
                + Thread.currentThread() + ") " + msg);
    }

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(SQLException exception) {
        sysout("start accMediaType=" + getAccMediaType());
        boolean counterAtStart;
        synchronized (this.counterSync) {
            counterAtStart = this.marker;
            this.marker = !this.marker;
        }
        do {
            TestUtils.sleep();
        } while (counterAtStart == this.marker);
        final MediaType accMediaType = getAccMediaType();
        sysout("middle accMediaType=" + accMediaType);
        synchronized (this.counterSync) {
            this.marker = !this.marker;
        }
        final ResponseBuilder rb = Response.serverError();
        rb.type(accMediaType);
        rb.entity("Database access error");
        sysout("end accMediaType=" + accMediaType);
        return rb.build();
    }
}