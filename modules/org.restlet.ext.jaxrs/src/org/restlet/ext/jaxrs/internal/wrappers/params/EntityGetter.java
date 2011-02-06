/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.ParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReaderSet;
import org.restlet.representation.Representation;

/**
 * An EntityGetter converts the given entity from the request to the type
 * requested by the resource method.<br>
 * This class is not used, if a subclass of {@link Representation} is requested,
 * see {@link ReprEntityGetter} and its subclasses.
 * 
 * @author Stephan Koops
 */
public class EntityGetter implements ParamGetter {

    private final Annotation[] annotations;

    /**
     * The class to convert to, directly as the type in the parameter list.
     */
    protected volatile Class<?> convToCl;

    private final Type convToGen;

    private final MessageBodyReaderSet mbrs;

    protected final ThreadLocalizedContext tlContext;

    EntityGetter(Class<?> convToCl, Type convToGen,
            ThreadLocalizedContext tlContext, MessageBodyReaderSet mbrs,
            Annotation[] annotations) {
        this.tlContext = tlContext;
        this.mbrs = mbrs;
        this.convToCl = convToCl;
        this.convToGen = convToGen;
        this.annotations = annotations;
    }

    /**
     * @throws ConvertRepresentationException
     * @throws WebApplicationException
     * @throws NoMessageBodyReaderException
     * @see IntoRrcInjector.AbstractInjectObjectGetter#getValue(String)
     */
    public Object getValue() throws ConvertRepresentationException, InvocationTargetException {
        final Request request = this.tlContext.get().getRequest();
        final Representation entity = request.getEntity();
        if (entity == null) {
            return null;
        }
        final MediaType mediaType = entity.getMediaType();
        final MessageBodyReader mbr = this.mbrs.getBestReader(this.convToCl,
                this.convToGen, this.annotations, mediaType);
        if (mbr == null) {
            throw new NoMessageBodyReaderException(mediaType, this.convToCl);
        }
        final MultivaluedMap<String, String> httpHeaders = Util
                .getJaxRsHttpHeaders(request);
        try {
            return mbr.readFrom(this.convToCl, this.convToGen,
                    this.annotations, mediaType, entity.getCharacterSet(), httpHeaders, entity
                            .getStream());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (IOException e) {
            throw ConvertRepresentationException.object(this.convToCl,
                    "the message body", e);
        } finally {
            entity.release();
        }
    }
}
