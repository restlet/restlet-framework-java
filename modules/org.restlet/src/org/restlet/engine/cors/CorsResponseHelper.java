package org.restlet.engine.cors;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.engine.util.SetUtils;

import java.util.Set;

/**
 * Helper to add CORS headers on HTTP response.
 * Support by default simple methods (HEAD, GET, POST).
 * For not simple methods, client send a preflight request with the method OPTIONS.
 *
 * @see <a href="http://www.w3.org/TR/cors">W3C CORS Specification</a>
 *
 * @author Manuel Boillod
 */
public class CorsResponseHelper {

    /**
     * If true, add 'Access-Control-Allow-Credentials' header.
     * Default is false.
     */
    public boolean allowCredentials = false;

    /**
     * Value of 'Access-Control-Allow-Origin' header.
     * Default is '*'.
     */
    public Set<String> allowOrigins = SetUtils.newHashSet("*");

    /**
     * If true, use value of 'Access-Control-Request-Method' request header
     * for 'Access-Control-Allow-Methods' response header.
     * Default is true.
     */
    public boolean allowOnlyRequestedMethod = true;

    /**
     * Value of 'Access-Control-Allow-Methods' response header.
     * Used only if {@link #allowOnlyRequestedMethod} is false.
     */
    public Set<Method> allowMethods = null;

    /**
     * If true, use value of 'Access-Control-Request-Headers' request header
     * for 'Access-Control-Allow-Headers' response header.
     * Default is true.
     */
    public boolean allowOnlyRequestedHeaders = true;

    /**
     * Value of 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowOnlyRequestedHeaders} is false.
     */
    public Set<String> allowHeaders = null;

    /**
     * Value of 'Access-Control-Expose-Headers' response header.
     */
    public Set<String> exposeHeaders = null;

    public CorsResponseHelper() {
    }

    /** Getter for {@link #allowCredentials} */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /** Setter for {@link #allowCredentials} */
    public CorsResponseHelper setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    /** Getter for {@link #allowOrigins} */
    public Set<String> getAllowOrigins() {
        return allowOrigins;
    }

    /** Setter for {@link #allowOrigins} */
    public CorsResponseHelper setAllowOrigins(Set<String> allowOrigins) {
        this.allowOrigins = allowOrigins;
        return this;
    }

    /** Getter for {@link #allowOnlyRequestedMethod} */
    public boolean isAllowOnlyRequestedMethod() {
        return allowOnlyRequestedMethod;
    }

    /** Setter for {@link #allowOnlyRequestedMethod} */
    public CorsResponseHelper setAllowOnlyRequestedMethod(boolean allowOnlyRequestedMethod) {
        this.allowOnlyRequestedMethod = allowOnlyRequestedMethod;
        return this;
    }

    /** Getter for {@link #allowMethods} */
    public Set<Method> getAllowMethods() {
        return allowMethods;
    }

    /** Setter for {@link #allowMethods} */
    public CorsResponseHelper setAllowMethods(Set<Method> allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    /** Getter for {@link #allowOnlyRequestedHeaders} */
    public boolean isAllowOnlyRequestedHeaders() {
        return allowOnlyRequestedHeaders;
    }

    /** Setter for {@link #allowOnlyRequestedHeaders} */
    public CorsResponseHelper setAllowOnlyRequestedHeaders(boolean allowOnlyRequestedHeaders) {
        this.allowOnlyRequestedHeaders = allowOnlyRequestedHeaders;
        return this;
    }

    /** Getter for {@link #allowHeaders} */
    public Set<String> getAllowHeaders() {
        return allowHeaders;
    }

    /** Setter for {@link #allowHeaders} */
    public CorsResponseHelper setAllowHeaders(Set<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }

    /** Getter for {@link #exposeHeaders} */
    public Set<String> getExposeHeaders() {
        return exposeHeaders;
    }

    /** Setter for {@link #exposeHeaders} */
    public CorsResponseHelper setExposeHeaders(Set<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
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

        if (!allowOrigins.contains("*") && !allowOrigins.contains(origin)) {
            // Origin not allowed
            return;
        }

        boolean isPreflightRequest = Method.OPTIONS.equals(request.getMethod());

        if (isPreflightRequest) {

            Method accessControlRequestMethod = request.getAccessControlRequestMethod();
            if (accessControlRequestMethod == null) {
                // Requested Method is required
                return;
            }

            Method requestedMethod = request.getAccessControlRequestMethod();
            if (requestedMethod == null) {
                return;
            }

            if (!allowOnlyRequestedMethod &&
                    (allowMethods == null || !allowMethods.contains(requestedMethod))) {
                //Method not allowed
                return;
            }

            Set<String> requestedHeaders = request.getAccessControlRequestHeaders();
            if (requestedHeaders == null) {
                requestedHeaders = SetUtils.newHashSet();
            }

            if (!allowOnlyRequestedHeaders &&
                    (allowHeaders == null || !isAllHeadersAllowed(allowHeaders, requestedHeaders))){
                //Headers not allowed
                return;
            }

            // Header 'Access-Control-Allow-Methods'
            response.setAccessControlAllowMethods(SetUtils.newHashSet(requestedMethod));

            // Header 'Access-Control-Allow-Headers'
            response.setAccessControlAllowHeaders(requestedHeaders);
        } else {
            //simple request

            // Header 'Access-Control-Expose-Headers'
            if (exposeHeaders != null && !exposeHeaders.isEmpty()) {
                response.setAccessControlExposeHeaders(exposeHeaders);
            }
        }

        // Header 'Access-Control-Allow-Credentials'
        if (allowCredentials) {
            response.setAccessControlAllowCredential(true);
        }

        // Header 'Access-Control-Allow-Origin'
        response.setAccessControlAllowOrigin(origin);
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
