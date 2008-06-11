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
package org.restlet.ext.jaxrs.internal.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.data.CharacterSet;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Provider for {@link String}s. Could also write other {@link CharSequence}s.
 * 
 * @author Stephan Koops
 */
@Provider
@ProduceMime( { "text/*", MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, "application/*+xml" })
@ConsumeMime( { "text/*", MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, "application/*+xml" })
public class StringProvider extends AbstractProvider<CharSequence> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(CharSequence t) {
        return t.length();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.isAssignableFrom(String.class);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public String readFrom(Class<CharSequence> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return Util.copyToStringBuilder(entityStream).toString();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(Object, Class, Type,
     *      Annotation[], MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(CharSequence charSequence, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        CharacterSet cs = Response.getCurrent().getEntity().getCharacterSet();
        InputStream inputStream = getInputStream(charSequence, cs.toString());
        Util.copyStream(inputStream, entityStream);
    }

    /**
     * Returns an {@link InputStream}, that returns the right encoded data
     * according to the given {@link CharacterSet}.
     * 
     * @param charSequ
     * @param charsetName
     *                see {@link String#getBytes(String)}
     * @return
     */
    private ByteArrayInputStream getInputStream(CharSequence charSequ,
            String charsetName) {
        byte[] bytes;
        String string = charSequ.toString();
        try {
            bytes = string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            try {
                bytes = string.getBytes(Util.JAX_RS_DEFAULT_CHARACTER_SET
                        .toString());
            } catch (UnsupportedEncodingException e1) {
                bytes = string.getBytes();
            }
        }
        return new ByteArrayInputStream(bytes);
    }
}