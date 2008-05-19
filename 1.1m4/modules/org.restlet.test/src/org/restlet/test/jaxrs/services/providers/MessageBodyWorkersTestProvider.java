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

import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWorkers;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.test.jaxrs.services.others.Person;

/**
 * This provider writes a Persons as XML (by JAXB) and with the
 * {@link CrazyTypeProvider}.<br>
 * (I've got no better idea for MessageBodyWorkers)
 * 
 * @author Stephan Koops
 */
@ProduceMime("text/crazy-person")
public class MessageBodyWorkersTestProvider implements
        MessageBodyWriter<Person> {

    @Context
    MessageBodyWorkers messageBodyWorkers;

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
    public void writeTo(Person t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        MessageBodyWriter<Person> mbw;
        mbw = messageBodyWorkers.getMessageBodyWriter(
                Person.class, Person.class, annotations,
                MediaType.APPLICATION_XML_TYPE);
        mbw.writeTo(t, Person.class, Person.class, annotations,
                MediaType.APPLICATION_XML_TYPE, httpHeaders, entityStream);

        MediaType mediaType2 = new MediaType("application", "crazyType");
        mbw = messageBodyWorkers.getMessageBodyWriter(Person.class, Person.class,
                annotations, mediaType2);
        mbw.writeTo(t, Person.class, Person.class, annotations, mediaType2,
                httpHeaders, entityStream);
    }
}