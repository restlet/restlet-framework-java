/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.service;

import java.util.Set;

import org.restlet.Context;
import org.restlet.engine.cors.CorsFilter;
import org.restlet.engine.util.SetUtils;
import org.restlet.routing.Filter;

/**
 * Application service that adds support of CORS. This service lets the target
 * resource specifies the allowed methods.
 * 
 * Example:
 * 
 * <pre>
 * CorsService corsService = new CorsService().setAllowedOrigins(
 *         new HashSet(Arrays.asList(&quot;http://server.com&quot;))).setAllowedCredentials(
 *         true);
 * </pre>
 * 
 * @author Manuel Boillod
 */
public class CorsService extends Service {

    /**
     * If true, copies the value of 'Access-Control-Request-Headers' request
     * header into the 'Access-Control-Allow-Headers' response header. If false,
     * use {@link #allowedHeaders}. Default is true.
     */
    public boolean allowAllRequestedHeaders = true;

    /**
     * If true, add 'Access-Control-Allow-Credentials' header. Default is false.
     */
    public boolean allowedCredentials = false;

    /**
     * The value of 'Access-Control-Allow-Headers' response header. Used only if
     * {@link #allowAllRequestedHeaders} is false.
     */
    public Set<String> allowedHeaders = null;

    /** The value of 'Access-Control-Allow-Origin' header. Default is '*'. */
    public Set<String> allowedOrigins = SetUtils.newHashSet("*");

    /** The value of 'Access-Control-Expose-Headers' response header. */
    public Set<String> exposedHeaders = null;

    /**
     * Constructor.
     */
    public CorsService() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public CorsService(boolean enabled) {
        super(enabled);
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new CorsFilter().setAllowedCredentials(allowedCredentials)
                .setAllowedOrigins(allowedOrigins)
                .setAllowAllRequestedHeaders(allowAllRequestedHeaders)
                .setAllowedHeaders(allowedHeaders)
                .setExposedHeaders(exposedHeaders);
    }

    /**
     * Returns the modifiable set of headers allowed by the actual request on
     * the current resource.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Allow-Headers" header.
     * 
     * @return The set of headers allowed by the actual request on the current
     *         resource.
     */
    public Set<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * Returns the URI an origin server allows for the requested resource. Use
     * "*" as a wildcard character.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Allow-Origin" header.
     * 
     * @return The origin allowed by the requested resource.
     */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Returns a modifiable whitelist of headers an origin server allows for the
     * requested resource.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Expose-Headers" header.
     * 
     * @return The set of headers an origin server allows for the requested
     *         resource.
     */
    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * If true, indicates that the value of 'Access-Control-Request-Headers'
     * request header will be copied into the 'Access-Control-Allow-Headers'
     * response header. If false, use {@link #allowedHeaders}.
     */
    public boolean isAllowAllRequestedHeaders() {
        return allowAllRequestedHeaders;
    }

    /**
     * If true, adds 'Access-Control-Allow-Credentials' header.
     * 
     * @return True, if the 'Access-Control-Allow-Credentials' header will be
     *         added.
     */
    public boolean isAllowedCredentials() {
        return allowedCredentials;
    }

    /**
     * If true, copies the value of 'Access-Control-Request-Headers' request
     * header into the 'Access-Control-Allow-Headers' response header. If false,
     * use {@link #allowedHeaders}.
     * 
     * @param allowAllRequestedHeaders
     *            True to copy the value of 'Access-Control-Request-Headers'
     *            request header into the 'Access-Control-Allow-Headers'
     *            response header. If false, use {@link #allowedHeaders}.
     * @return Itself for chaining methods calls.
     */
    public CorsService setAllowAllRequestedHeaders(
            boolean allowAllRequestedHeaders) {
        this.allowAllRequestedHeaders = allowAllRequestedHeaders;
        return this;
    }

    /**
     * If true, adds 'Access-Control-Allow-Credentials' header.
     * 
     * @param allowedCredentials
     *            True to add the 'Access-Control-Allow-Credentials' header.
     * @return Itself for chaining methods calls.
     */
    public CorsService setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
        return this;
    }

    /**
     * Sets the value of the 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowAllRequestedHeaders} is false.
     * 
     * @param allowedHeaders
     *            The value of 'Access-Control-Allow-Headers' response header.
     * @return Itself for chaining methods calls.
     */
    public CorsService setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
        return this;
    }

    /**
     * Sets the value of 'Access-Control-Allow-Origin' header.
     * 
     * @param allowedOrigins
     *            The value of 'Access-Control-Allow-Origin' header.
     * @return Itself for chaining methods calls.
     */
    public CorsService setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

    /**
     * Sets the value of 'Access-Control-Expose-Headers' response header.
     * 
     * @param exposedHeaders
     *            The value of 'Access-Control-Expose-Headers' response header.
     * @return Itself for chaining methods calls.
     */
    public CorsService setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
        return this;
    }
}
