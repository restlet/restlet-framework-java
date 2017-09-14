/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.services.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.restlet.test.ext.jaxrs.services.others.Person;

/**
 * This provider writes a Persons as XML (by JAXB) and with the
 * {@link TextCrazyPersonProvider}.<br>
 * (I've got no better idea for Providers)
 * 
 * @author Stephan Koops
 */
@Produces("application/crazy-person")
@Provider
public class AppCrazyPersonProvider implements MessageBodyWriter<Person> {

    @Context
    Providers providers;

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(Person t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
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
        mbw = this.providers.getMessageBodyWriter(Person.class, Person.class,
                annotations, MediaType.APPLICATION_XML_TYPE);
        mbw.writeTo(t, Person.class, Person.class, annotations,
                MediaType.APPLICATION_XML_TYPE, httpHeaders, entityStream);

        final MediaType mediaType2 = new MediaType("text", "crazy-person");
        mbw = this.providers.getMessageBodyWriter(Person.class, Person.class,
                annotations, mediaType2);
        mbw.writeTo(t, Person.class, Person.class, annotations, mediaType2,
                httpHeaders, entityStream);
    }
}
