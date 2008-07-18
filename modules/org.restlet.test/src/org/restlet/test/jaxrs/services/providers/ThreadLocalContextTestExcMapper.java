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
package org.restlet.test.jaxrs.services.providers;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * 
 */
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