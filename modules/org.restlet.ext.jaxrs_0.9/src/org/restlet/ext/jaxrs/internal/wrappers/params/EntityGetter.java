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
package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.ParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReaderSet;
import org.restlet.resource.Representation;

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
     * @see IntoRrcInjector.AbstractInjectObjectGetter#getValue()
     */
    @SuppressWarnings("unchecked")
    public Object getValue() throws ConvertRepresentationException {
        final Request request = this.tlContext.get().getRequest();
        final Representation entity = request.getEntity();
        if (entity == null) {
            return null;
        }
        final MediaType mediaType = entity.getMediaType();
        final MessageBodyReader<?> mbr = this.mbrs.getBestReader(this.convToCl,
                this.convToGen, this.annotations, mediaType);
        if (mbr == null) {
            throw new NoMessageBodyReaderException(mediaType, this.convToCl);
        }
        final MultivaluedMap<String, String> httpHeaders = Util
                .getJaxRsHttpHeaders(request);
        try {
            final javax.ws.rs.core.MediaType jaxRsMediaType = Converter
                    .toJaxRsMediaType(mediaType, entity.getCharacterSet());
            return mbr.readFrom((Class) this.convToCl, this.convToGen,
                    this.annotations, jaxRsMediaType, httpHeaders, entity
                            .getStream());
        } catch (final WebApplicationException wae) {
            throw wae;
        } catch (final IOException e) {
            throw ConvertRepresentationException.object(this.convToCl,
                    "the message body", e);
        } finally {
            entity.release();
        }
    }
}