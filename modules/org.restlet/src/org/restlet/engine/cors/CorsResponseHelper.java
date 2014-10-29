package org.restlet.engine.cors;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

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
     * Returns a default instance of {@link #CorsResponseHelper()}.
     * This instance is shared across whole application, so do not update its configuration.
     */
    public static final CorsResponseHelper DEFAULT = new CorsResponseHelper();

    /**
     * If true, add 'Access-Control-Allow-Credentials' header.
     * Default is true.
     */
    public boolean allowCredentials = true;

    /**
     * Value of 'Access-Control-Allow-Origin' header.
     * Default is '*'.
     */
    public String allowOrigin = "*";

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

    /** Getter for {@link #allowOrigin} */
    public String getAllowOrigin() {
        return allowOrigin;
    }

    /** Setter for {@link #allowOrigin} */
    public CorsResponseHelper setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
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

    /**
     * Add CORS headers to response if request has header 'Origin'.
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response
     */
    public void addCorsResponseHeaderIfCorsRequest(Request request, Response response) {
        if (isCorsRequest(request)) {
            addCorsResponseHeaders(request, response);
        }
    }

    /**
     * Test if request is a CORS request, ie if request has header 'Origin'.
     *
     * @param request
     *            The request to handle.
     */
    public boolean isCorsRequest(Request request) {
        return request.getHeaders().getValues("Origin") != null;
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
        // Header 'Access-Control-Allow-Origin'
        response.setAccessControlAllowOrigin(allowOrigin);

        // Header 'Access-Control-Allow-Credentials'
        if (allowCredentials) {
            response.setAccessControlAllowCredential(true);
        }

        // Header 'Access-Control-Allow-Methods'
        if (allowOnlyRequestedMethod) {
            if (request == null) {
                throw new RuntimeException("If allowOnlyRequestedMethod is true, it requires the request parameter");
            }
            Set<Method> accessControlRequestMethods = request.getAccessControlRequestMethod();
            if (!accessControlRequestMethods.isEmpty()) {
                response.setAccessControlAllowMethods(accessControlRequestMethods);
            }
        } else {
            if (allowMethods != null) {
                response.setAccessControlAllowMethods(allowMethods);
            }
        }

        // Header 'Access-Control-Allow-Headers'
        if (allowOnlyRequestedHeaders) {
            if (request == null) {
                throw new RuntimeException("If allowOnlyRequestedHeaders is true, it requires the request parameter");
            }
            Set<String> accessControlRequestHeaders = request.getAccessControlRequestHeaders();
            if (!accessControlRequestHeaders.isEmpty()) {
                response.setAccessControlAllowHeaders(accessControlRequestHeaders);
            }
        } else {
            if (allowHeaders != null) {
                response.setAccessControlAllowHeaders(allowHeaders);
            }
        }
    }
}
