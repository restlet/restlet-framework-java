/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package com.noelios.restlet.util;

import javax.net.ssl.SSLContext;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

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
public abstract class SslContextFactory {

    /**
     * Creates a configured and initialised SSLContext.
     * 
     * @return A configured and initialised SSLContext.
     * @throws Exception
     */
    public abstract SSLContext createSslContext() throws Exception;

    /**
     * Initialize the factory with the given connector parameters.
     * 
     * @param parameters
     *            The connector parameters.
     */
    public abstract void init(Series<Parameter> parameters);

}
