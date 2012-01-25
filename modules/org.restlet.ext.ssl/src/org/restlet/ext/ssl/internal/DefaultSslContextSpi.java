/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.ssl.internal;

import java.security.KeyManagementException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.restlet.ext.ssl.DefaultSslContextFactory;

/**
 * Default SSL context SPI capable or setting additional properties on the
 * created SSL engines and socket factories.
 * 
 * @author Jerome Louvel
 */
public class DefaultSslContextSpi extends SSLContextSpi {

    /** The parent SSL context factory. */
    private final DefaultSslContextFactory contextFactory;

    /** The wrapped SSL context. */
    private final SSLContext wrappedContext;

    /**
     * Constructor.
     * 
     * @param contextFactory
     *            The parent SSL context factory.
     * @param wrappedContext
     *            The wrapped SSL context.
     */
    public DefaultSslContextSpi(DefaultSslContextFactory contextFactory,
            SSLContext wrappedContext) {
        this.contextFactory = contextFactory;
        this.wrappedContext = wrappedContext;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine() {
        SSLEngine result = getWrappedContext().createSSLEngine();
        initEngine(result);
        return result;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(String peerHost, int peerPort) {
        SSLEngine result = getWrappedContext().createSSLEngine(peerHost,
                peerPort);
        initEngine(result);
        return result;
    }

    /**
     * Initializes the SSL engine with additional parameters from the SSL
     * context factory.
     * 
     * @param sslEngine
     *            The SSL engine to initialize.
     */
    protected void initEngine(SSLEngine sslEngine) {
        sslEngine.setNeedClientAuth(getContextFactory()
                .isNeedClientAuthentication());
        sslEngine.setWantClientAuth(getContextFactory()
                .isWantClientAuthentication());
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return getWrappedContext().getClientSessionContext();
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        return getWrappedContext().getServerSessionContext();
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        return getWrappedContext().getServerSocketFactory();
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
        return getWrappedContext().getSocketFactory();
    }

    @Override
    protected void engineInit(KeyManager[] km, TrustManager[] tm,
            SecureRandom random) throws KeyManagementException {
        getWrappedContext().init(km, tm, random);
    }

    /**
     * Returns the parent SSL context factory.
     * 
     * @return The parent SSL context factory.
     */
    protected DefaultSslContextFactory getContextFactory() {
        return contextFactory;
    }

    /**
     * Returns the wrapped SSL context.
     * 
     * @return The wrapped SSL context.
     */
    protected SSLContext getWrappedContext() {
        return wrappedContext;
    }

}
