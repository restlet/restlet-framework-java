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

package org.restlet.ext.ssl;

import javax.net.ssl.SSLContext;

import org.jsslutils.sslcontext.SSLContextFactory;

import org.restlet.data.Parameter;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.util.Series;


/**
 * This SslContextFactory is a wrapper for the SSLContextFactory of <a
 * href="http://code.google.com/p/jsslutils/">jSSLutils</a>.
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class JsslutilsSslContextFactory extends SslContextFactory {
    /**
     * The wrapped SSLContextFactory.
     */
    private final SSLContextFactory sslContextFactory;

    /**
     * Builds JsslutilsSslContextFactory that wraps an instance of
     * jsslutils.sslcontext.SSLContextFactory.
     * 
     * @param sslContextFactory
     *            SSLContextFactory (from jSSLutils) to wrap.
     */
    public JsslutilsSslContextFactory(SSLContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
    }

    /**
     * Creates a configured and initialised SSLContext by delegating the call to
     * the SSLContextFactory with which the target instance was built. Please
     * set the SSLContext protocol in that factory; it is 'SSLv3' in version 0.3
     * of jSSLutils.
     * 
     * @see SSLContextFactory#buildSSLContext()
     */
    @Override
    public SSLContext createSslContext() throws Exception {
        return this.sslContextFactory.buildSSLContext();
    }

    /**
     * Returns the wrapped SSLContextFactory with which this instance was built.
     * 
     * @return the wrapped SSLContextFactory.
     */
    public SSLContextFactory getSslContextFactory() {
        return this.sslContextFactory;
    }

    @Override
    public void init(Series<Parameter> parameters) {
    }
}
