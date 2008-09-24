/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.data.CharacterSet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.util.ByteUtils;

/**
 * This ProviderWrapper is used to read directly from a {@link Reader}.
 * 
 * @author Stephan Koops
 * @see BufferedReaderProvider
 */
@Provider
public class ReaderProvider extends AbstractProvider<Reader> {

    /**
     * Returns a Reader wrapping the given entity stream, with respect to the
     * {@link CharacterSet} of the entity of the current {@link Request}, or
     * UTF-8 if no character set was given or if it is not available
     */
    static Reader getReader(InputStream entityStream) {
        final Representation entity = Request.getCurrent().getEntity();
        CharacterSet cs;
        if (entity != null) {
            cs = entity.getCharacterSet();
            if (cs == null) {
                cs = Util.JAX_RS_DEFAULT_CHARACTER_SET;
            }
        } else {
            cs = Util.JAX_RS_DEFAULT_CHARACTER_SET;
        }
        try {
            return ByteUtils.getReader(entityStream, cs);
        } catch (final UnsupportedEncodingException e) {
            try {
                return ByteUtils.getReader(entityStream, null);
            } catch (final UnsupportedEncodingException e2) {
                throw new WebApplicationException(500);
            }
        }
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Reader t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public Reader readFrom(Class<Reader> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return getReader(entityStream);
    }

    @Override
    protected Class<?> supportedClass() {
        return Reader.class;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Type, Annotation[], MediaType,
     *      MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(Reader reader, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        final CharacterSet cs = Response.getCurrent().getEntity()
                .getCharacterSet();
        Util.copyStream(ByteUtils.getStream(reader, cs), entityStream);
        // NICE testen charset for ReaderProvider.writeTo(..) ?
    }
}