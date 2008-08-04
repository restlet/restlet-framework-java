/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.restlet.data.MediaType;

/**
 * Contains a List of wrapped {@link javax.ws.rs.ext.MessageBodyReader}s.
 * 
 * @author Stephan Koops
 */
public interface MessageBodyReaderSet {

    /**
     * Returns the best {@link MessageBodyReader} in this Set.
     * 
     * @param paramType
     * @param genericType
     * @param annotations
     * @param mediaType
     *            The {@link MediaType}, that should be supported.
     * 
     * @return the {@link MessageBodyReader}, that best matches the given
     *         criteria, or null if no matching MessageBodyReader could be
     *         found.
     */
    public MessageBodyReader getBestReader(Class<?> paramType,
            Type genericType, Annotation[] annotations, MediaType mediaType);
}