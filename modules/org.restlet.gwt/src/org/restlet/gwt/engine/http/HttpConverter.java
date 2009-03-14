/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.gwt.engine.http;

import org.restlet.gwt.Context;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.util.Series;

/**
 * Converter between high-level and low-level HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class HttpConverter {
    /** The context. */
    private Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use.
     */
    public HttpConverter(Context context) {
        this.context = context;
    }

    /**
     * Adds additional headers if they are non-standard headers.
     * 
     * @param existingHeaders
     *            The headers to update.
     * @param additionalHeaders
     *            The headers to add.
     */
    public void addAdditionalHeaders(Series<Parameter> existingHeaders,
            Series<Parameter> additionalHeaders) {
        if (additionalHeaders != null) {
            for (final Parameter param : additionalHeaders) {
                if (param.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_ACCEPT)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ACCEPT_CHARSET)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ACCEPT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ACCEPT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ALLOW)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONNECTION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_LENGTH)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_TYPE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_DATE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ETAG)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_EXPIRES)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_HOST)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_IF_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_IF_MODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_IF_NONE_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_IF_UNMODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_LAST_MODIFIED)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_REFERRER)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_SERVER)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_SET_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_SET_COOKIE2)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_USER_AGENT)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_VARY)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_WWW_AUTHENTICATE)) {
                    // Standard headers that can't be overriden
                    System.err
                            .println("Addition of the standard header \""
                                    + param.getName()
                                    + "\" is not allowed. Please use the Restlet API instead.");
                } else if (param.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_AGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_ACCEPT_RANGES)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CACHE_CONTROL)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_MD5)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_CONTENT_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_EXPECT)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_FROM)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_IF_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_MAX_FORWARDS)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_PRAGMA)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_PROXY_AUTHENTICATE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_PROXY_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_RETRY_AFTER)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_TRAILER)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_TRANSFER_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_TRANSFER_EXTENSION)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_UPGRADE)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_VIA)
                        || param.getName().equalsIgnoreCase(
                                HttpConstants.HEADER_WARNING)) {
                    // Standard headers can't shouldn't be overriden
                    System.err
                            .println("Addition of the standard header \""
                                    + param.getName()
                                    + "\" is discouraged. Future versions of the Restlet API will directly support it.");
                    existingHeaders.add(param);
                } else {
                    existingHeaders.add(param);
                }
            }
        }
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
    }

}
