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
    public boolean exposeOnlyRequestedHeader = true;

    /**
     * Value of 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #exposeOnlyRequestedHeader} is false.
     */
    public String exposeHeaders = null;

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
                    .setExposeOnlyRequestedHeader(exposeOnlyRequestedHeader)
                    .setExposeHeaders(exposeHeaders);
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

    /** Getter for {@link #exposeOnlyRequestedHeader} */
    public boolean isExposeOnlyRequestedHeader() {
        return exposeOnlyRequestedHeader;
    }

    /** Setter for {@link #exposeOnlyRequestedHeader} */
    public CorsFilter setExposeOnlyRequestedHeader(boolean exposeOnlyRequestedHeader) {
        this.exposeOnlyRequestedHeader = exposeOnlyRequestedHeader;
        return this;
    }

    /** Getter for {@link #exposeHeaders} */
    public String getExposeHeaders() {
        return exposeHeaders;
    }

    /** Setter for {@link #exposeHeaders} */
    public CorsFilter setExposeHeaders(String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
        return this;
    }
}
