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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.data.MediaType;

/**
 * @author Stephan Koops
 */
public interface MessageBodyWorkers {

    /**
     * @param mediaType 
     * @param type 
     * @param genericType 
     * @param annotations 
     * @param <T> 
     * @return 
     * @see javax.ws.rs.ext.MessageBodyWorkers#getMessageBodyReaders(javax.ws.rs.core.MediaType,
     *      Class, Type, Annotation[])
     */
    public <T> List<MessageBodyReader<T>> getMessageBodyReaders(
            MediaType mediaType, Class<T> type, Type genericType,
            Annotation[] annotations);

    /**
     * @param mediaType 
     * @param type 
     * @param genericType 
     * @param annotations 
     * @param <T> 
     * @return 
     * @see javax.ws.rs.ext.MessageBodyWorkers#getMessageBodyWriters(javax.ws.rs.core.MediaType,
     *      Class, Type, Annotation[])
     */
    public <T> List<MessageBodyWriter<T>> getMessageBodyWriters(
            MediaType mediaType, Class<T> type, Type genericType,
            Annotation[] annotations);
}
