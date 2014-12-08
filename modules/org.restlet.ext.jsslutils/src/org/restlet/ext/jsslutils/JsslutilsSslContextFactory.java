/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jsslutils;

import javax.net.ssl.SSLContext;

import org.jsslutils.sslcontext.SSLContextFactory;
import org.restlet.data.Parameter;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.util.Series;

/**
 * This SslContextFactory is a wrapper for the SSLContextFactory of <a
 * href="http://code.google.com/p/jsslutils/">jSSLutils</a>.
 * 
 * @author Bruno Harbulot
 * @deprecated Not actively developed anymore.
 */
@Deprecated
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
     * Creates a configured and initialized SSLContext by delegating the call to
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
