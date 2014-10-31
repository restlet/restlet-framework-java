package org.restlet.engine.cors;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.util.SetUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Helper to add CORS headers on HTTP response.
 * In CORS specification, not simple methods should send a preflight request with the method OPTIONS.
 * Simple methods are HEAD, GET and POST.
 *
 * @see <a href="http://www.w3.org/TR/cors">W3C CORS Specification</a>
 *
 * @author Manuel Boillod
 */
public class CorsResponseHelper {

    private static Logger LOGGER = Context.getCurrentLogger();

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
     * for 'Access-Control-Allow-Headers' response header. If false, use
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

    public CorsResponseHelper() {
    }

    /** Getter for {@link #allowedCredentials} */
    public boolean isAllowedCredentials() {
        return allowedCredentials;
    }

    /** Setter for {@link #allowedCredentials} */
    public CorsResponseHelper setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
        return this;
    }

    /** Getter for {@link #allowedOrigins} */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /** Setter for {@link #allowedOrigins} */
    public CorsResponseHelper setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

    /** Getter for {@link #allowAllRequestedHeaders} */
    public boolean isAllowAllRequestedHeaders() {
        return allowAllRequestedHeaders;
    }

    /** Setter for {@link #allowAllRequestedHeaders} */
    public CorsResponseHelper setAllowAllRequestedHeaders(boolean allowAllRequestedHeaders) {
        this.allowAllRequestedHeaders = allowAllRequestedHeaders;
        return this;
    }

    /** Getter for {@link #allowedHeaders} */
    public Set<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /** Setter for {@link #allowedHeaders} */
    public CorsResponseHelper setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
        return this;
    }

    /** Getter for {@link #exposedHeaders} */
    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /** Setter for {@link #exposedHeaders} */
    public CorsResponseHelper setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
        return this;
    }

    /**
     * Add CORS headers to response
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response
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

            //Default OPTIONS method in a server resource returns a
            // {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED} if the method is not implemented
            // or a {@link Status#SUCCESS_NO_CONTENT} or a {@link Status#SUCCESS_NO_CONTENT} if
            // the method is implemented and the call succeed.
            // Other status are considered as error.

            // Preflight request returns a 200 status except if server resource method .
            // If the preflight request is not allowed, CORS response headers will not be added.
            if (Status.SUCCESS_OK.equals(response.getStatus())
                    || Status.SUCCESS_NO_CONTENT.equals(response.getStatus())
                    || Status.CLIENT_ERROR_METHOD_NOT_ALLOWED.equals(response.getStatus())) {
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
                //Method not allowed
                LOGGER.fine("The CORS preflight request ask for methods not allowed in header 'Access-Control-Request-Method'");
                return;
            }

            Set<String> requestedHeaders = request.getAccessControlRequestHeaders();
            if (requestedHeaders == null) {
                requestedHeaders = SetUtils.newHashSet();
            }

            if (!allowAllRequestedHeaders &&
                    (allowedHeaders == null || !isAllHeadersAllowed(allowedHeaders, requestedHeaders))){
                //Headers not allowed
                LOGGER.fine("The CORS preflight request ask for headers not allowed in header 'Access-Control-Request-Headers'");
                return;
            }

            // Header 'Access-Control-Allow-Methods'
            response.setAccessControlAllowMethods(allowedMethods);

            // Header 'Access-Control-Allow-Headers'
            response.setAccessControlAllowHeaders(requestedHeaders);
        } else {
            //simple request

            // Header 'Access-Control-Expose-Headers'
            if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
                response.setAccessControlExposeHeaders(exposedHeaders);
            }
        }

        // Header 'Access-Control-Allow-Credentials'
        if (allowedCredentials) {
            response.setAccessControlAllowCredential(true);
        }

        // Header 'Access-Control-Allow-Origin'
        if (!allowedCredentials && allowedOrigins.contains("*")) {
            response.setAccessControlAllowOrigin("*");
        } else {
            response.setAccessControlAllowOrigin(origin);
        }
    }

    /**
     * Returns true if all requested headers are allowed (case-insensitive)
     * @param allowHeaders
     *      The allowed headers
     * @param requestedHeaders
     *      The requested headers
     * @return True if all requested headers are allowed (case-insensitive)
     */
    private boolean isAllHeadersAllowed(Set<String> allowHeaders, Set<String> requestedHeaders) {
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


}
