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
package com.noelios.restlet.ext.ssl;

import javax.net.ssl.SSLContext;

import jsslutils.sslcontext.SSLContextFactory;

import com.noelios.restlet.util.SslContextFactory;

/**
 * This SslContextFactory is a wrapper for the SSLContextFactory of <a
 * href="http://code.google.com/p/jsslutils/">jSSLutils</a>.
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class JsslutilsSslContextFactory implements SslContextFactory {
    /**
     * The wrapped SSLContextFactory.
     */
    private SSLContextFactory sslContextFactory;

    /**
     * Builds JsslutilsSslContextFactory that wraps an instance of
     * jsslutils.sslcontext.SSLContextFactory.
     * 
     * @param sslContextFactory
     *                SSLContextFactory (from jSSLutils) to wrap.
     */
    public JsslutilsSslContextFactory(SSLContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
    }

    /**
     * Returns the wrapped SSLContextFactory with which this instance was built.
     * 
     * @return the wrapped SSLContextFactory.
     */
    public SSLContextFactory getSslContextFactory() {
        return this.sslContextFactory;
    }

    /**
     * Creates a configured and initialised SSLContext by delegating the call to
     * the SSLContextFactory with which the target instance was built. Please
     * set the SSLContext protocol in that factory; it is 'SSLv3' in version 0.3
     * of jSSLutils.
     * 
     * @see SSLContextFactory#buildSSLContext()
     */
    public SSLContext createSslContext() throws Exception {
        return this.sslContextFactory.buildSSLContext();
    }
}
