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
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.restlet.test.jaxrs.services.others.Person;

/**
 * @author Stephan Koops
 */
@Produces("text/html")
@Provider
public class ContextResolverTestWriter implements MessageBodyWriter<Person> {

    @Context
    void setProviders(Providers providers) {
        this.contextResolver = providers.getContextResolver(
                BaseUriContext.class, Object.class, MediaType.TEXT_HTML_TYPE);
    }

    private ContextResolver<BaseUriContext> contextResolver;

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
        return this.contextResolver.getContext(type) != null;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(Person person, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        final BaseUriContext context = this.contextResolver.getContext(type);
        final OutputStreamWriter writer = new OutputStreamWriter(entityStream);
        writer.write("<html><head></head><body>\n");
        writer.write("The virtual presence of <b>");
        writer.write(person.getFirstname() + " " + person.getLastname());
        writer.write("</b> is: ");
        writer.write("<a href=\"");
        writer.write(context.getBaseUri());
        writer.write("persons/");
        writer.write(person.getLastname() + "/" + person.getFirstname());
        writer.write("\">");
        writer.write(context.getBaseUri());
        writer.write("persons/");
        writer.write(person.getLastname() + "/" + person.getFirstname());
        writer.write("</a>");
        writer.write("</html>");
        writer.flush();
    }
}