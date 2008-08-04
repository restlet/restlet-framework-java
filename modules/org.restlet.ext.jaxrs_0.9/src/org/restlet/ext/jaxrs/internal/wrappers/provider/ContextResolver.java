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

import javax.ws.rs.core.MediaType;

/**
 * Wrapper vor a JAX-RS {@link javax.ws.rs.ext.ContextResolver}.
 * 
 * @author Stephan Koops
 */
public interface ContextResolver {
    /**
     * Returns the wrapped ContextResolver
     * 
     * @return the wrapped ContextResolver
     */
    public javax.ws.rs.ext.ContextResolver<?> getContextResolver();

    /**
     * Checks, if the wrapped ContextResolver supports the given
     * {@link MediaType}.
     * 
     * @param mediaType
     *                the {@link MediaType} to check for support.
     * @return true, if the requested {@link MediaType} is supported, otherwise
     *         false.
     */
    public boolean supportsWrite(MediaType mediaType);
}
