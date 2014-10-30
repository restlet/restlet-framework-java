package org.restlet.engine.cors;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Filter;

import java.util.Set;

/**
 * Filter that add CORS headers on HTTP response.
 * OPTIONS Methods are intercepted by this filter and
 * Resource OPTIONS methods are never called.
 *
 * Example:
 * <pre>
 * Router router = new Router(getContext());
 * CorsFilter corsFilter = new CorsFilter(getContext(), router)
 *  .setAllowMethods("GET,POST")
 *  .setAllowCredentials(false);
 * </pre>
 *
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
    public boolean allowOnlyRequestedHeader = true;

    /**
     * Value of 'Access-Control-Allow-Headers' response header.
     * Used only if {@link #allowOnlyRequestedHeader} is false.
     */
    public Set<String> allowHeaders = null;

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
     * If current request method is OPTIONS, do not call doHandle().
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (Method.OPTIONS.equals(request.getMethod())) {
            return SKIP;
        } else {
            return super.beforeHandle(request, response);
        }
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
    protected CorsResponseHelper getCorsResponseHelper() {
        if (corsResponseHelper == null) {
            corsResponseHelper = new CorsResponseHelper()
                    .setAllowCredentials(allowCredentials)
                    .setAllowOrigin(allowOrigin)
                    .setAllowOnlyRequestedMethod(allowOnlyRequestedMethod)
                    .setAllowMethods(allowMethods)
                    .setAllowOnlyRequestedMethod(allowOnlyRequestedMethod)
                    .setAllowOnlyRequestedHeaders(allowOnlyRequestedHeader)
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

    /** Getter for {@link #allowOnlyRequestedMethod} */
    public boolean isAllowOnlyRequestedMethod() {
        return allowOnlyRequestedMethod;
    }

    /** Setter for {@link #allowOnlyRequestedMethod} */
    public CorsFilter setAllowOnlyRequestedMethod(boolean allowOnlyRequestedMethod) {
        this.allowOnlyRequestedMethod = allowOnlyRequestedMethod;
        return this;
    }

    /** Getter for {@link #allowMethods} */
    public Set<Method> getAllowMethods() {
        return allowMethods;
    }

    /** Setter for {@link #allowMethods} */
    public CorsFilter setAllowMethods(Set<Method> allowMethods) {
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
    public Set<String> getAllowHeaders() {
        return allowHeaders;
    }

    /** Setter for {@link #allowHeaders} */
    public CorsFilter setAllowHeaders(Set<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }
}
