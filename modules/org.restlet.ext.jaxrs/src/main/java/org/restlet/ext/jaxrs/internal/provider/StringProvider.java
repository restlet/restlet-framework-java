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

package org.restlet.ext.jaxrs.internal.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.representation.Representation;

/**
 * ProviderWrapper for {@link String}s. Could also write other
 * {@link CharSequence}s.
 * 
 * @author Stephan Koops
 */
@Provider
@Produces("*/*")
@Consumes("*/*")
public class StringProvider extends AbstractProvider<CharSequence> {

    /**
     * Returns the given entity as byte array converted by the given character
     * set.
     * 
     * @param entity
     * @param charsetName
     * @return the given entity as byte array converted by the given character
     *         set.
     */
    private byte[] getByteArray(CharSequence entity, String charsetName) {
        final String string = entity.toString();
        try {
            if (charsetName != null)
                return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            // try with default character set, see below
        }
        try {
            return string.getBytes(Util.JAX_RS_DEFAULT_CHARACTER_SET_AS_STRING);
        } catch (UnsupportedEncodingException e1) {
            return string.getBytes();
        }
        // NICE cache for some seconds
    }

    /**
     * @return the character set of the current entity, or null, if no entity or
     *         no character set is available.
     */
    private CharacterSet getCurrentRequestEntityCharacterSet() {
        Representation entity = Request.getCurrent().getEntity();

        if (entity == null)
            return null;

        return entity.getCharacterSet();
    }

    /**
     * @return the character set of the current entity, or null, if no entity or
     *         no character set is available.
     */
    private String getCurrentResponseEntityCharset() {
        String result = null;
        Response rsp = Response.getCurrent();

        if (rsp == null) {
            Context.getCurrentLogger().warning(
                    "Unable to find the current response");
        } else {
            Representation entity = Response.getCurrent().getEntity();

            if (entity == null)
                return null;

            CharacterSet characterSet = entity.getCharacterSet();

            if (characterSet == null)
                return null;

            result = characterSet.toString();
        }

        return result;

    }

    /**
     * Returns an {@link InputStream}, that returns the right encoded data
     * according to the given {@link CharacterSet}.
     * 
     * @param charSequ
     * @param charsetName
     *            see {@link String#getBytes(String)}
     * @return
     */
    private ByteArrayInputStream getInputStream(CharSequence charSequ,
            String charsetName) {
        byte[] bytes = getByteArray(charSequ, charsetName);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(CharSequence entity, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return getByteArray(entity, getCurrentResponseEntityCharset()).length;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(String.class);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public String readFrom(Class<CharSequence> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return IoUtils.toString(entityStream,
                getCurrentRequestEntityCharacterSet());
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
        String charset = getCurrentResponseEntityCharset();
        InputStream inputStream = getInputStream(charSequence, charset);
        IoUtils.copy(inputStream, entityStream);
    }
}
