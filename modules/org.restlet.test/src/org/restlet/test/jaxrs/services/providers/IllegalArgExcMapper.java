/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

    public static final int STATUS = 599;

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