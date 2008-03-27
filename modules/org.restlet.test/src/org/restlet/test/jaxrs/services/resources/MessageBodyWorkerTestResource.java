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
package org.restlet.test.jaxrs.services.resources;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.test.jaxrs.services.tests.PathParamTest;

/**
 * <b>this test is not used yet !!</b>
 * 
 * @author Stephan Koops
 * @see PathParamTest
 * @see PathParam
 */
@Path("workers")
public class MessageBodyWorkerTestResource {

    @Context
    MessageBodyWorkers messageBodyWorkers;

    @POST
    public Response post(InputStream inputStream, @Context
    HttpHeaders headers, @Context
    UriInfo uriInfo) throws IOException {
        List<MessageBodyReader<String>> mbrs = messageBodyWorkers
                .getMessageBodyReaders(headers.getMediaType(), String.class,
                        String.class, new Annotation[0]);
        // REQUEST provide an interface as return type of MessageBodyWork.get*()
        // to not need twice give the type, genType, MediaType and annotations?
        // REQUEST MessBodyWorkers are very complicated, IMO. app developer
        // needs own implementation of MultivaluedMap. (headers are unmodifiable
        // which is good)
        String toCreate = mbrs.get(0).readFrom(String.class, String.class,
                headers.getMediaType(), new Annotation[0],
                headers.getRequestHeaders(), inputStream);
        System.out.println(toCreate);
        URI location = uriInfo.getAbsolutePathBuilder().path("5").build();
        return Response.created(location).build();
    }
}