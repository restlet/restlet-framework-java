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

import org.restlet.test.ext.jaxrs.services.others.Person;

/**
 * @author Stephan Koops
 */
@Produces("text/html")
@Provider
public class ContextResolverTestWriter implements MessageBodyWriter<Person> {

    @Context
    void setProviders(Providers providers) {
        this.contextResolver = providers.getContextResolver(
                BaseUriContext.class, MediaType.TEXT_HTML_TYPE);
        if (this.contextResolver == null)
            throw new RuntimeException("No Context Resolver found");
    }

    private ContextResolver<BaseUriContext> contextResolver;

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(Object, Class, Type,
     *      Annotation[], MediaType)
     */
    public long getSize(Person t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[], MediaType)
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
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
