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
package org.restlet.test.jaxrs.services.providers;

import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.restlet.test.jaxrs.ExceptionMappersTest;
import org.restlet.test.jaxrs.services.resources.ExcMapperTestResource;
import org.restlet.test.jaxrs.services.tests.ExcMapperTest;

/**
 * @author Stephan Koops
 * @see ExcMapperTestResource
 * @see ExcMapperTest
 * @see ExceptionMappersTest
 */
@Provider
public class IllegalArgExcMapper implements
        ExceptionMapper<IllegalArgumentException> {

    public static final int STATUS = 6887987;

    /** public for direct set from test class */
    @Context
    public HttpHeaders httpHeaders;

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(IllegalArgumentException exception) {
        String entity = "Could not convert:\n" + exception.getClass().getName()
                + ": " + exception.getMessage();
        final ResponseBuilder rb = Response.status(STATUS);
        final List<MediaType> accMediaTypes = this.httpHeaders
                .getAcceptableMediaTypes();
        if (accMediaTypes.contains(MediaType.TEXT_HTML_TYPE)) {
            rb.type(MediaType.TEXT_HTML_TYPE);
            entity = "<html><head><title>invalid argument</title></head>"
                    + "<boy><h1>Sorry</h1><p>" + entity + "</p></body></html>";
        } else {
            rb.type(MediaType.TEXT_PLAIN_TYPE);
        }
        return rb.entity(entity).build();
    }
}