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
package com.noelios.restlet.util;

import javax.net.ssl.SSLContext;

/**
 * This is an abstract factory that produces configured and initialised
 * instances of SSLContext. Concrete implementations of SslContextFactory must
 * implement {@link #createSslContext()}, which should typically consist of:
 * 
 * <pre>
 *    SSLContext sslContext = SSLContext.getInstance(...);
 *    ...
 *    sslContext.init(..., ..., ...);
 *    return sslContext;
 * </pre>
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 * @see SSLContext
 */
public interface SslContextFactory {

    /**
     * Creates a configured and initialised SSLContext.
     * 
     * @return A configured and initialised SSLContext.
     * @throws Exception
     */
    public SSLContext createSslContext() throws Exception;

}
