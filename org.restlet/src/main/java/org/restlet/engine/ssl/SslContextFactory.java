/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.ssl;

import javax.net.ssl.SSLContext;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * This is an abstract factory that produces configured and initialized instances of SSLContext.
 * Concrete implementations of SslContextFactory must implement {@link #createSslContext()}, which
 * should typically consist of:
 *
 * <pre>
 *    SSLContext sslContext = SSLContext.getInstance(...);
 *    ...
 *    sslContext.init(..., ..., ...);
 *    return sslContext;
 * </pre>
 *
 * @author Bruno Harbulot
 * @see SSLContext
 */
public abstract class SslContextFactory {

    /**
     * Creates a configured and initialized SSLContext.
     *
     * @return A configured and initialized SSLContext.
     * @throws Exception
     */
    public abstract SSLContext createSslContext() throws Exception;

    /**
     * Initialize the factory with the given connector parameters.
     *
     * @param parameters The connector parameters.
     */
    public abstract void init(Series<Parameter> parameters);
}
