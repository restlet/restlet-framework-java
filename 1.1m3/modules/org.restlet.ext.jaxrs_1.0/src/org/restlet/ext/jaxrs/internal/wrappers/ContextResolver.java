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

/**
 * Wraps a {@link javax.ws.rs.ext.ContextResolver}.
 * 
 * @author Stephan Koops
 * @param <T> the java type to get its context.
 * @see javax.ws.rs.ext.ContextResolver
 */
public interface ContextResolver<T> {
    /**
     * Get a context of type <code>T</code> that is applicable to the supplied
     * type.
     * 
     * @param type
     *                the class of object for which a context is desired
     * @return a context for the supplied type or <code>null<code> if a 
     * context for the supplied type is not available from this provider.
     * @see javax.ws.rs.ext.ContextResolver#getContext(Class)
     */
    public T getContext(Class<?> type);

    /**
     * @return
     */
    public javax.ws.rs.ext.ContextResolver<?> getJaxRsContextResolver();
}