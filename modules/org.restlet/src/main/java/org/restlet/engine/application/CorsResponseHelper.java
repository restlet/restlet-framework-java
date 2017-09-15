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

package org.restlet.engine.application;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.util.SetUtils;

/**
 * Helps to generate response CORS headers.<br>
 * The CORS specification defines a subset of methods qualified as simple HEAD,
 * GET and POST. Any other methods should send a preflight request with the
 * method OPTIONS.
 * 
 * @see <a href="http://www.w3.org/TR/cors">W3C CORS Specification</a>
 * @see <a href="http://www.w3.org/TR/cors/#simple-method">Simple methods</a>
 * 
 * @author Manuel Boillod
 */
public class CorsResponseHelper {

    private static Logger LOGGER = Context.getCurrentLogger();

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
     * The value of 'Access-Control-Allow-Headers' response header. Used only if {@link #allowAllRequestedHeaders} is
     * false.
     */
    public Set<String> allowedHeaders = null;

    /** The value of 'Access-Control-Allow-Origin' header. Default is '*'. */
    public Set<String> allowedOrigins = SetUtils.newHashSet("*");

    /** The value of 'Access-Control-Expose-Headers' response header. */
    public Set<String> exposedHeaders = null;

    /** The value of 'Access-Control-Max-Age' response header. Default is that the header is not set. */
    public int maxAge = -1;

    /**
     * Adds CORS headers to the given response.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The response.
     */
    public void addCorsResponseHeaders(Request request, Response response) {

        String origin = request.getHeaders().getFirstValue("Origin", true);

        if (origin == null) {
            // Not a CORS request
            return;
        }

        Set<Method> allowedMethods = new HashSet<>(response.getAllowedMethods());
        // Header 'Allow' is not relevant in CORS request.
        response.getAllowedMethods().clear();

        if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin)) {
            // Origin not allowed
            LOGGER.fine("Origin " + origin + " not allowed for CORS request");
            return;
        }

        boolean isPreflightRequest = Method.OPTIONS.equals(request.getMethod());

        if (isPreflightRequest) {

            // Default OPTIONS method in a server resource returns a
            // {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED} if the method is
            // not implemented
            // or a {@link Status#SUCCESS_NO_CONTENT} or a {@link
            // Status#SUCCESS_NO_CONTENT} if
            // the method is implemented and the call succeed.
            // Other status are considered as error.

            // Preflight request returns a 200 status except if server resource
            // method .
            // If the preflight request is not allowed, CORS response headers
            // will not be added.
            if (Status.SUCCESS_OK.equals(response.getStatus())
                    || Status.SUCCESS_NO_CONTENT.equals(response.getStatus())
                    || Status.CLIENT_ERROR_METHOD_NOT_ALLOWED.equals(response
                            .getStatus())) {
                response.setStatus(Status.SUCCESS_OK);
            } else {
                LOGGER.fine("The CORS preflight request failed in server resource.");
                return;
            }

            Method requestedMethod = request.getAccessControlRequestMethod();
            if (requestedMethod == null) {
                // Requested Method is required
                LOGGER.fine("A CORS preflight request should specified header 'Access-Control-Request-Method'");
                return;
            }

            if (!allowedMethods.contains(requestedMethod)) {
                // Method not allowed
                LOGGER.fine("The CORS preflight request ask for methods not allowed in header 'Access-Control-Request-Method'");
                return;
            }

            Set<String> requestedHeaders = request
                    .getAccessControlRequestHeaders();
            if (requestedHeaders == null) {
                requestedHeaders = SetUtils.newHashSet();
            }

            if (!allowAllRequestedHeaders
                    && (allowedHeaders == null || !isAllHeadersAllowed(
                            allowedHeaders, requestedHeaders))) {
                // Headers not allowed
                LOGGER.fine("The CORS preflight request ask for headers not allowed in header 'Access-Control-Request-Headers'");
                return;
            }

            // Header 'Access-Control-Allow-Methods'
            response.setAccessControlAllowMethods(allowedMethods);

            // Header 'Access-Control-Allow-Headers'
            response.setAccessControlAllowHeaders(requestedHeaders);
            
            if (getMaxAge() > 0) {
                response.setAccessControlMaxAge(getMaxAge());
            }
        } else {
            // simple request

            // Header 'Access-Control-Expose-Headers'
            if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
                response.setAccessControlExposeHeaders(exposedHeaders);
            }
        }

        // Header 'Access-Control-Allow-Credentials'
        if (allowedCredentials) {
            response.setAccessControlAllowCredentials(true);
        }

        // Header 'Access-Control-Allow-Origin'
        if (!allowedCredentials && allowedOrigins.contains("*")) {
            response.setAccessControlAllowOrigin("*");
        } else {
            response.setAccessControlAllowOrigin(origin);
        }
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
     * Returns true if all requested headers are allowed (case-insensitive).
     * 
     * @param allowHeaders
     *            The allowed headers.
     * @param requestedHeaders
     *            The requested headers.
     * @return True if all requested headers are allowed (case-insensitive).
     */
    private boolean isAllHeadersAllowed(Set<String> allowHeaders,
            Set<String> requestedHeaders) {
        for (String requestedHeader : requestedHeaders) {
            boolean headerAllowed = false;
            for (String allowHeader : allowHeaders) {
                if (allowHeader.equalsIgnoreCase(requestedHeader)) {
                    headerAllowed = true;
                    break;
                }
            }
            if (!headerAllowed) {
                return false;
            }
        }
        return true;
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
     * Returns true if the request is a CORS request.
     * 
     * @param request
     *            The current request.
     * @return true if the request is a CORS request.
     */
    public boolean isCorsRequest(Request request) {
        return request.getHeaders().getFirstValue("Origin", true) != null;
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
     */
    public void setAllowAllRequestedHeaders(
            boolean allowAllRequestedHeaders) {
        this.allowAllRequestedHeaders = allowAllRequestedHeaders;
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
     * Used only if {@link #allowAllRequestedHeaders} is false.
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

}
