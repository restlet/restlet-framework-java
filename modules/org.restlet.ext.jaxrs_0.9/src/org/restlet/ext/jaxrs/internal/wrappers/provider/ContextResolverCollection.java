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

import java.util.Collection;

import javax.ws.rs.ext.ContextResolver;

/**
 * Collection, if multiple context resolvers are possible for injection.
 * 
 * @author Stephan Koops
 */
public class ContextResolverCollection implements ContextResolver<Object> {

    private Collection<ContextResolver<?>> resolvers;

    /**
     * @param resolvers
     */
    public ContextResolverCollection(Collection<ContextResolver<?>> resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getContext(Class<?> type) {
        for (ContextResolver<?> cr : resolvers) {
            Object context = cr.getContext(type);
            if (context != null)
                return context;
        }
        return null;
    }
}