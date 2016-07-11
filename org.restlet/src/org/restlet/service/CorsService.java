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

package org.restlet.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.engine.application.CorsFilter;
import org.restlet.engine.util.SetUtils;
import org.restlet.routing.Filter;

/**
 * Application service that adds support of CORS. This service lets the target
 * resource specifies the allowed methods.
 * 
 * Example:
 * 
 * <pre>
 * CorsService corsService = new CorsService();
 * corsService.setAllowedOrigins(new HashSet(Arrays.asList(&quot;http://server.com&quot;)));
 * corsService.setAllowedCredentials(true);
 * </pre>
 * 
 * @author Manuel Boillod
 */
public class CorsService extends Service {

    /**
     * If true, add 'Access-Control-Allow-Credentials' header. Default is false.
     */
    private boolean allowedCredentials = false;

    /**
     * The value of 'Access-Control-Allow-Headers' response header. Used only if {@link #allowingAllRequestedHeaders} is
     * false.
     */
    private Set<String> allowedHeaders = null;

    /** The value of 'Access-Control-Allow-Origin' header. Default is '*'. */
    private Set<String> allowedOrigins = SetUtils.newHashSet("*");

    /**
     * If true, copies the value of 'Access-Control-Request-Headers' request
     * header into the 'Access-Control-Allow-Headers' response header. If false,
     * use {@link #allowedHeaders}. Default is true.
     */
    private boolean allowingAllRequestedHeaders = true;

    /**
     * The set of methods allowed by default, used when {@link #skippingResourceForCorsOptions} is turned on. By
     * default: GET, PUT, POST, DELETE, PATCH.
     */
    private Set<Method> defaultAllowedMethods = new HashSet<>(Arrays.asList(Method.GET, Method.POST, Method.PUT,
            Method.DELETE, Method.PATCH));

    /** The value of 'Access-Control-Expose-Headers' response header. */
    private Set<String> exposedHeaders = null;

    /** The value of 'Access-Control-Max-Age' response header. Default is that the header is not set. */
    private int maxAge = -1;

    /**
     * If true, the filter does not call the server resource for OPTIONS method
     * of CORS request and set Access-Control-Allow-Methods header with
     * the default methods cf {@link #getDefaultAllowedMethods()}. Default is false.
     */
    private boolean skippingResourceForCorsOptions = false;

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
                .setAllowingAllRequestedHeaders(allowingAllRequestedHeaders)
                .setAllowedHeaders(allowedHeaders)
                .setExposedHeaders(exposedHeaders)
                .setSkippingResourceForCorsOptions(skippingResourceForCorsOptions)
                .setDefaultAllowedMethods(getDefaultAllowedMethods())
                .setMaxAge(maxAge);
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
     * Returns the list of methods allowed by default, used when {@link #skippingResourceForCorsOptions} is turned on.
     * 
     * @return The list of methods allowed by default, used when {@link #skippingResourceForCorsOptions} is turned on.
     */
    public Set<Method> getDefaultAllowedMethods() {
        return defaultAllowedMethods;
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
     * Indicates how long (in seconds) the results of a preflight request can be cached in a preflight result cache.<br>
     * In case of a negative value, the results of a preflight request is not meant to be cached.<br>
     * Note that when used with HTTP connectors, this property maps to the "Access-Control-Max-Age" header.
     * 
     * @return Indicates how long the results of a preflight request can be cached in a preflight result cache.
     */
    public int getMaxAge() {
        return maxAge;
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
     * If true, indicates that the value of 'Access-Control-Request-Headers'
     * request header will be copied into the 'Access-Control-Allow-Headers'
     * response header. If false, use {@link #allowedHeaders}.
     * 
     * @return True to copy the value
     */
    public boolean isAllowingAllRequestedHeaders() {
        return allowingAllRequestedHeaders;
    }

    /**
     * If true, the filter does not call the server resource for OPTIONS method
     * of CORS request and set Access-Control-Allow-Methods header with
     * the default methods. Default is false.
     * 
     * @return True if the filter does not call the server resource for
     *         OPTIONS method of CORS request.
     */
    public boolean isSkippingResourceForCorsOptions() {
        return skippingResourceForCorsOptions;
    }

    /**
     * If true, adds 'Access-Control-Allow-Credentials' header.
     * 
     * @param allowedCredentials
     *            True to add the 'Access-Control-Allow-Credentials' header.
     */
    public void setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
    }

    /**
     * Sets the value of the 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowingAllRequestedHeaders} is false.
     * 
     * @param allowedHeaders
     *            The value of 'Access-Control-Allow-Headers' response header.
     */
    public void setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * Sets the value of 'Access-Control-Allow-Origin' header.
     * 
     * @param allowedOrigins
     *            The value of 'Access-Control-Allow-Origin' header.
     */
    public void setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * If true, copies the value of 'Access-Control-Request-Headers' request
     * header into the 'Access-Control-Allow-Headers' response header. If false,
     * use {@link #allowedHeaders}.
     * 
     * @param allowingAllRequestedHeaders
     *            True to copy the value of 'Access-Control-Request-Headers'
     *            request header into the 'Access-Control-Allow-Headers'
     *            response header. If false, use {@link #allowedHeaders}.
     */
    public void setAllowingAllRequestedHeaders(boolean allowingAllRequestedHeaders) {
        this.allowingAllRequestedHeaders = allowingAllRequestedHeaders;
    }

    /**
     * Sets the list of methods allowed by default, used when {@link #skippingResourceForCorsOptions} is turned on.
     * 
     * @param defaultAllowedMethods
     *            The list of methods allowed by default, used when {@link #skippingResourceForCorsOptions} is turned
     *            on.
     */
    public void setDefaultAllowedMethods(Set<Method> defaultAllowedMethods) {
        this.defaultAllowedMethods = defaultAllowedMethods;
    }

    /**
     * Sets the value of 'Access-Control-Expose-Headers' response header.
     * 
     * @param exposedHeaders
     *            The value of 'Access-Control-Expose-Headers' response header.
     */
    public void setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * Sets the value of 'Access-Control-Max-Age' response header.<br>
     * In case of negative value, the header is not set.
     * 
     * @param maxAge
     *            The value of 'Access-Control-Max-Age' response header.
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Sets the value of skipResourceForCorsOptions field.
     * 
     * @param skipResourceForCorsOptions
     *            True if the filter does not call the server resource for
     *            OPTIONS method of CORS request.
     */
    public void setSkippingResourceForCorsOptions(boolean skipResourceForCorsOptions) {
        this.skippingResourceForCorsOptions = skipResourceForCorsOptions;
    }
}
