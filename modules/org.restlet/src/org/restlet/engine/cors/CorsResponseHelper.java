package org.restlet.engine.cors;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.Resource;
import org.restlet.util.Series;

/**
 * Helper to add CORS headers on HTTP response.
 *
 * @author Manuel Boillod
 */
public class CorsResponseHelper {

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
     * If true, use value of 'Access-Control-Request-Methods' request header
     * for 'Access-Control-Allow-Methods' response header.
     * Default is true.
     */
    public boolean allowOnlyRequestedMethods = true;

    /**
     * Value of 'Access-Control-Allow-Methods' response header.
     * Used only if {@link #allowOnlyRequestedMethods} is false.
     */
    public String allowMethods = null;

    /**
     * If true, use value of 'Access-Control-Request-Headers' request header
     * for 'Access-Control-Allow-Headers' response header.
     * Default is true.
     */
    public boolean allowOnlyRequestedHeader = true;

    /**
     * Value of 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowOnlyRequestedHeader} is false.
     */
    public String allowHeaders = null;

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

    /** Getter for {@link #allowOnlyRequestedMethods} */
    public boolean isAllowOnlyRequestedMethods() {
        return allowOnlyRequestedMethods;
    }

    /** Setter for {@link #allowOnlyRequestedMethods} */
    public CorsResponseHelper setAllowOnlyRequestedMethods(boolean allowOnlyRequestedMethods) {
        this.allowOnlyRequestedMethods = allowOnlyRequestedMethods;
        return this;
    }

    /** Getter for {@link #allowMethods} */
    public String getAllowMethods() {
        return allowMethods;
    }

    /** Setter for {@link #allowMethods} */
    public CorsResponseHelper setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    /** Getter for {@link #allowOnlyRequestedHeader} */
    public boolean isAllowOnlyRequestedHeader() {
        return allowOnlyRequestedHeader;
    }

    /** Setter for {@link #allowOnlyRequestedHeader} */
    public CorsResponseHelper setAllowOnlyRequestedHeader(boolean allowOnlyRequestedHeader) {
        this.allowOnlyRequestedHeader = allowOnlyRequestedHeader;
        return this;
    }

    /** Getter for {@link #allowHeaders} */
    public String getAllowHeaders() {
        return allowHeaders;
    }

    /** Setter for {@link #allowHeaders} */
    public CorsResponseHelper setAllowHeaders(String allowHeaders) {
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
     * @return
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
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) response.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        if (headers == null) {
            headers = new Series<Header>(Header.class);
            response.getAttributes()
                    .put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
        }

        // Header 'Access-Control-Allow-Origin'
        headers.set("Access-Control-Allow-Origin", allowOrigin);

        // Header 'Access-Control-Allow-Credentials'
        if (allowCredentials) {
            headers.set("Access-Control-Allow-Credentials", "true");
        }

        // Header 'Access-Control-Allow-Methods'
        if (allowOnlyRequestedMethods) {
            if (request == null) {
                throw new RuntimeException("If allowOnlyRequestedMethods is true, it requires the request parameter");
            }
            String accessControlRequestMethods = request.getHeaders().getValues("Access-Control-Request-Methods");
            if (accessControlRequestMethods != null) {
                headers.set("Access-Control-Allow-Methods", accessControlRequestMethods);
            }
        } else {
            if (allowMethods != null) {
                headers.set("Access-Control-Allow-Methods", allowMethods);
            }
        }

        // Header 'Access-Control-Allow-Headers'
        if (allowOnlyRequestedHeader) {
            if (request == null) {
                throw new RuntimeException("If allowOnlyRequestedHeader is true, it requires the request parameter");
            }
            String accessControlRequestHeaders = request.getHeaders().getValues("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                headers.set("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
        } else {
            if (allowHeaders != null) {
                headers.set("Access-Control-Allow-Headers", allowHeaders);
            }
        }
    }
}
