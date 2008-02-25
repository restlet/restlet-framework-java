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

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 * 
 */
@Provider
@ProduceMime("text/*")
@ConsumeMime("text/*")
public class StringProvider extends AbstractProvider<CharSequence> {

    // REQUESTED JSR311: why @Provider? Jersey Providers are also not annotated.
    // . . . . . . . (check before email)
    // REQUESTED JSR311: javax.transform.Source statt javax.xml.transform.Source
    // TODO wie soll man application/*+xml feststellen?

    /**
     * 
     */
    public StringProvider() {
        // nothing to do
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(CharSequence t) {
        return t.length();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class)
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(Class)
     */
    @Override
    protected boolean isReadableAndWriteable(Class<?> type, Type genericType, Annotation[] annotations) {
        return CharSequence.class.isAssignableFrom(type);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object,
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.OutputStream)
     */
    @Override
    public void writeTo(CharSequence cs, Type genericType,
            Annotation[] annotations,
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        byte[] array = cs.toString().getBytes();
        entityStream.write(array);
    }

    @Override
    public CharSequence readFrom(Class<CharSequence> type, Type genericType,
            MediaType mediaType, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        throw new NotYetImplementedException();
    }
}