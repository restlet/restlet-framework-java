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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import javax.ws.rs.ext.ContextResolver;

/**
 * This {@link ContextResolver} returns ever null. Is is used as default, if no
 * one was found.
 * 
 * @author Stephan Koops
 * @param <T>
 * @see ContextResolver
 */
public class ReturnNullContextResolver<T> implements ContextResolver<T> {

    /**
     * The instance.
     * @see #get()
     */
    public static final ReturnNullContextResolver<Object> INSTANCE = new ReturnNullContextResolver<Object>();

    private ReturnNullContextResolver() {
    }

    /**
     * Returns the singelton instance.
     * 
     * @param <A>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <A> ReturnNullContextResolver<A> get() {
        return (ReturnNullContextResolver) INSTANCE;
    }

    /**
     * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
     */
    public T getContext(Class<?> type) {
        return null;
    }
}