package org.restlet.engine.cors;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.util.SetUtils;
import org.restlet.routing.Filter;

import java.util.Set;

/**
 * Filter that add CORS headers on HTTP response.
 * The resource methods specifies the allowed methods.
 *
 * Example:
 * <pre>
 * Router router = new Router(getContext());
 * CorsFilter corsFilter = new CorsFilter(getContext(), router)
 *  .setAllowedOrigins(new HashSet(Arrays.asList("http://server.com")))
 *  .setAllowedCredentials(true);
 * </pre>
 *
 * @author Manuel Boillod
 */
public class CorsFilter extends Filter {

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
        corsResponseHelper.addCorsResponseHeaders(request, response);
    }

    /**
     * Returns a lazy-initialized instance of {@link org.restlet.engine.cors.CorsResponseHelper}.
     */
    protected CorsResponseHelper getCorsResponseHelper() {
        if (corsResponseHelper == null) {
            corsResponseHelper = new CorsResponseHelper()
                    .setAllowedCredentials(allowedCredentials)
                    .setAllowedOrigins(allowedOrigins)
                    .setAllowAllRequestedHeaders(allowAllRequestedHeaders)
                    .setAllowedHeaders(allowedHeaders)
                    .setExposedHeaders(exposedHeaders);
        }
        return corsResponseHelper;
    }

    // Getters & Setters

    /** Getter for {@link #allowedCredentials} */
    public boolean isAllowedCredentials() {
        return allowedCredentials;
    }

    /** Setter for {@link #allowedCredentials} */
    public CorsFilter setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
        return this;
    }

    /** Getter for {@link #allowedOrigins} */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /** Setter for {@link #allowedOrigins} */
    public CorsFilter setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

    /** Getter for {@link #allowAllRequestedHeaders} */
    public boolean isAllowAllRequestedHeaders() {
        return allowAllRequestedHeaders;
    }

    /** Setter for {@link #allowAllRequestedHeaders} */
    public CorsFilter setAllowAllRequestedHeaders(boolean allowAllRequestedHeaders) {
        this.allowAllRequestedHeaders = allowAllRequestedHeaders;
        return this;
    }

    /** Getter for {@link #allowedHeaders} */
    public Set<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /** Setter for {@link #allowedHeaders} */
    public CorsFilter setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
        return this;
    }

    /** Getter for {@link #exposedHeaders} */
    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /** Setter for {@link #exposedHeaders} */
    public CorsFilter setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
        return this;
    }
}
