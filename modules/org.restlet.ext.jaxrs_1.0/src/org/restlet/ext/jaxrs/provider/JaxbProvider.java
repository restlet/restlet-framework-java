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
package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stephan Koops
 * 
 */
@Provider
public class JaxbProvider extends AbstractJaxbProvider<Object> {

    private Logger logger = Logger.getLogger(JaxbProvider.class.getName());

    @Override
    Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#readFrom(java.lang.Class,
     *      Type, javax.ws.rs.core.MediaType, Annotation[],
     *      javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return unmarshal(type, entityStream);
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#writeTo(java.lang.Object,
     *      Type, Annotation[], javax.ws.rs.core.MediaType,
     *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(Object object, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpResponseHeaders,
            OutputStream entityStream) throws IOException {
        marshal(object, entityStream);
    }
}