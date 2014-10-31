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

import org.restlet.Context;
import org.restlet.engine.cors.CorsFilter;
import org.restlet.engine.util.SetUtils;
import org.restlet.routing.Filter;

import java.util.Set;

/**
 * Application service that add CORS headers on HTTP response.
 * The resource methods specifies the allowed methods.
 *
 * Example:
 * <pre>
 * CorsService corsService = new CorsService()
 *  .setAllowedOrigins(new HashSet(Arrays.asList("http://server.com")))
 *  .setAllowedCredentials(true);
 * </pre>
 *
 * @author Manuel Boillod
 */
public class CorsService extends Service {

    /**
     * If true, add 'Access-Control-Allow-Credentials' header.
     * Default is false.
     */
    public boolean allowedCredentials = false;

    /**
     * Value of 'Access-Control-Allow-Origin' header.
     * Default is '*'.
     */
    public Set<String> allowedOrigins = SetUtils.newHashSet("*");

    /**
     * If true, use value of 'Access-Control-Request-Headers' request header
     * for 'Access-Control-Allow-Headers' response header.  If false, use
     * {@link #allowedHeaders}.
     * Default is true.
     */
    public boolean allowAllRequestedHeaders = true;

    /**
     * Value of 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowAllRequestedHeaders} is false.
     */
    public Set<String> allowedHeaders = null;

    /**
     * Value of 'Access-Control-Expose-Headers' response header.
     */
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
        return new CorsFilter()
                .setAllowedCredentials(allowedCredentials)
                .setAllowedOrigins(allowedOrigins)
                .setAllowAllRequestedHeaders(allowAllRequestedHeaders)
                .setAllowedHeaders(allowedHeaders)
                .setExposedHeaders(exposedHeaders);
    }

    // Getters & Setters

    /** Getter for {@link #allowedCredentials} */
    public boolean isAllowedCredentials() {
        return allowedCredentials;
    }

    /** Setter for {@link #allowedCredentials} */
    public CorsService setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
        return this;
    }

    /** Getter for {@link #allowedOrigins} */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /** Setter for {@link #allowedOrigins} */
    public CorsService setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

    /** Getter for {@link #allowAllRequestedHeaders} */
    public boolean isAllowAllRequestedHeaders() {
        return allowAllRequestedHeaders;
    }

    /** Setter for {@link #allowAllRequestedHeaders} */
    public CorsService setAllowAllRequestedHeaders(boolean allowAllRequestedHeaders) {
        this.allowAllRequestedHeaders = allowAllRequestedHeaders;
        return this;
    }

    /** Getter for {@link #allowedHeaders} */
    public Set<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /** Setter for {@link #allowedHeaders} */
    public CorsService setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
        return this;
    }

    /** Getter for {@link #exposedHeaders} */
    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /** Setter for {@link #exposedHeaders} */
    public CorsService setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
        return this;
    }
}
