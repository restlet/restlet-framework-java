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

package org.restlet.ext.jaxrs.internal.wrappers.provider;

import javax.ws.rs.core.MediaType;

/**
 * Wrapper for an initialized JAX-RS {@link javax.ws.rs.ext.ContextResolver}.
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
