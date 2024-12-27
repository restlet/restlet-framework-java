/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.httpclient.internal;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.params.HttpParams;

/**
 * Factory that creates {@link IgnoreCookieSpec} instances.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next minor release in favor or Jetty extension.
 */
@Deprecated
public class IgnoreCookieSpecFactory implements CookieSpecFactory {

    /**
     * Creates a new instance of {@link IgnoreCookieSpec}.
     * 
     * @param params
     *            The parameters are ignored.
     * @return The created instance.
     */
    public CookieSpec newInstance(final HttpParams params) {
        return new IgnoreCookieSpec();
    }

}
