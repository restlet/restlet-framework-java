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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.jaxrs.services.resources.GenericTypeResource;
import org.restlet.test.jaxrs.services.tests.GenericTypeTestCase;

/**
 * @author Stephan Koops
 * @see GenericTypeResource
 * @see GenericTypeTestCase
 */
@Provider
@Produces("text/plain")
public class GenericTypeMBW implements MessageBodyWriter<List<String>> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(List<String> t) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return (List.class.isAssignableFrom(type) && Util.getGenericClass(
                genericType).equals(String.class));
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(List<String> t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        for (final String s : t) {
            entityStream.write(s.getBytes());
            entityStream.write('\n');
        }
    }
}