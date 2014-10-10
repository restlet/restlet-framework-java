package org.restlet.engine.cors;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

/**
 * Filter that add CORS headers on HTTP response.
 *
 * Example:
 * <pre>
 * Router router = new Router(getContext());
 * CorsFilter corsFilter = new CorsFilter(getContext(), router)
 *  .setAllowMethods("GET,POST")
 *  .setAllowCredentials(false);
 * </pre>
 * @author Manuel Boillod
 */
public class CorsFilter extends Filter {

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

    private CorsResponseHelper corsResponseHelper;

    public CorsFilter() {
    }

    public CorsFilter(Context context) {
        super(context);
    }

    public CorsFilter(Context context, Restlet next) {
        super(context, next);
    }

    /**
     * Add CORS headers to response
     * @param request
     *            The request to handle.
     * @param response
     *            The response
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        CorsResponseHelper corsResponseHelper = getCorsResponseHelper();
        corsResponseHelper.addCorsResponseHeaderIfCorsRequest(request, response);
    }

    /**
     * Returns a lazy-initialized instance of {@link org.restlet.engine.cors.CorsResponseHelper}.
     */
    private CorsResponseHelper getCorsResponseHelper() {
        if (corsResponseHelper == null) {
            corsResponseHelper = new CorsResponseHelper()
                    .setAllowCredentials(allowCredentials)
                    .setAllowOrigin(allowOrigin)
                    .setAllowOnlyRequestedMethods(allowOnlyRequestedMethods)
                    .setAllowMethods(allowMethods)
                    .setAllowOnlyRequestedMethods(allowOnlyRequestedMethods)
                    .setAllowOnlyRequestedHeader(allowOnlyRequestedHeader)
                    .setAllowHeaders(allowHeaders);
        }
        return corsResponseHelper;
    }

    // Getters & Setters

    /** Getter for {@link #allowCredentials} */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /** Setter for {@link #allowCredentials} */
    public CorsFilter setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    /** Getter for {@link #allowOrigin} */
    public String getAllowOrigin() {
        return allowOrigin;
    }

    /** Setter for {@link #allowOrigin} */
    public CorsFilter setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
        return this;
    }

    /** Getter for {@link #allowOnlyRequestedMethods} */
    public boolean isAllowOnlyRequestedMethods() {
        return allowOnlyRequestedMethods;
    }

    /** Setter for {@link #allowOnlyRequestedMethods} */
    public CorsFilter setAllowOnlyRequestedMethods(boolean allowOnlyRequestedMethods) {
        this.allowOnlyRequestedMethods = allowOnlyRequestedMethods;
        return this;
    }

    /** Getter for {@link #allowMethods} */
    public String getAllowMethods() {
        return allowMethods;
    }

    /** Setter for {@link #allowMethods} */
    public CorsFilter setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    /** Getter for {@link #allowOnlyRequestedHeader} */
    public boolean isAllowOnlyRequestedHeader() {
        return allowOnlyRequestedHeader;
    }

    /** Setter for {@link #allowOnlyRequestedHeader} */
    public CorsFilter setAllowOnlyRequestedHeader(boolean allowOnlyRequestedHeader) {
        this.allowOnlyRequestedHeader = allowOnlyRequestedHeader;
        return this;
    }

    /** Getter for {@link #allowHeaders} */
    public String getAllowHeaders() {
        return allowHeaders;
    }

    /** Setter for {@link #allowHeaders} */
    public CorsFilter setAllowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }
}
