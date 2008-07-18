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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.resources.OwnProviderTestService;

/**
 * @author Stephan Koops
 * @see OwnProviderTestService
 */
@Provider
@Produces("application/crazyType")
public class CrazyTypeProvider implements MessageBodyWriter<Person> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(Person t) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return Person.class.isAssignableFrom(type);
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(Person person, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> responseHeaders,
            OutputStream entityStream) throws IOException {
        entityStream.write(person.getFirstname().getBytes());
        entityStream.write(' ');
        entityStream.write(person.getLastname().getBytes());
        entityStream.write(" is crazy.".getBytes());
        final Object h1v = responseHeaders.getFirst("h1");
        if (h1v != null) {
            entityStream.write("\nHeader value for name h1 is ".getBytes());
            entityStream.write(h1v.toString().getBytes());
        } else {
            entityStream.write("\nNo header value for name h1".getBytes());
        }
    }
}