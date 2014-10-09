package org.restlet.engine.cors;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

/**
 * @author Manuel Boillod
 */
public class CorsFilter extends Filter {

    private boolean allowCredentials = true;

    private String allowOrigin = "*";

    private boolean allowOnlyRequestedMethods = true;

    private String allowMethods = null;

    private boolean exposeOnlyRequestedHeader = true;

    private String exposeHeaders = null;

    public CorsFilter() {
    }

    public CorsFilter(Context context) {
        super(context);
    }

    public CorsFilter(Context context, Restlet next) {
        super(context, next);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        CorsResponseHelper corsResponseHelper = new CorsResponseHelper()
                .setAllowCredentials(allowCredentials)
                .setAllowOrigin(allowOrigin)
                .setAllowOnlyRequestedMethods(allowOnlyRequestedMethods)
                .setAllowMethods(allowMethods)
                .setAllowOnlyRequestedMethods(allowOnlyRequestedMethods)
                .setExposeOnlyRequestedHeader(exposeOnlyRequestedHeader)
                .setExposeHeaders(exposeHeaders);
        corsResponseHelper.addCorsResponseHeaders(request, response);
    }

    // Getters & Setters

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public String getAllowOrigin() {
        return allowOrigin;
    }

    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    public boolean isAllowOnlyRequestedMethods() {
        return allowOnlyRequestedMethods;
    }

    public void setAllowOnlyRequestedMethods(boolean allowOnlyRequestedMethods) {
        this.allowOnlyRequestedMethods = allowOnlyRequestedMethods;
    }

    public String getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
    }

    public boolean isExposeOnlyRequestedHeader() {
        return exposeOnlyRequestedHeader;
    }

    public void setExposeOnlyRequestedHeader(boolean exposeOnlyRequestedHeader) {
        this.exposeOnlyRequestedHeader = exposeOnlyRequestedHeader;
    }

    public String getExposeHeaders() {
        return exposeHeaders;
    }

    public void setExposeHeaders(String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }
}
