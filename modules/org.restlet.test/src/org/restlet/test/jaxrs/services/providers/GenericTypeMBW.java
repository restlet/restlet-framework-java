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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.jaxrs.services.resources.GenericTypeResource;
import org.restlet.test.jaxrs.services.tests.GenericTypeTestCase;

/**
 * @author Stephan Koops
 * @see GenericTypeResource
 * @see GenericTypeTestCase
 */
@Provider
@Produces("text/plain")
public class GenericTypeMBW implements MessageBodyWriter<List<String>> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(List<String> t) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return (List.class.isAssignableFrom(type) && Util.getGenericClass(
                genericType).equals(String.class));
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(List<String> t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        for (final String s : t) {
            entityStream.write(s.getBytes());
            entityStream.write('\n');
        }
    }
}