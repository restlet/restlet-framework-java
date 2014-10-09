package org.restlet.engine.cors;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.Resource;
import org.restlet.util.Series;

/**
 * @author Manuel Boillod
 */
public class CorsResponseHelper {

    public boolean allowCredentials = true;

    public String allowOrigin = "*";

    public boolean allowOnlyRequestedMethods = true;

    public String allowMethods = null;

    public boolean exposeOnlyRequestedHeader = true;

    public String exposeHeaders = null;

    public CorsResponseHelper() {
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public CorsResponseHelper setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public String getAllowOrigin() {
        return allowOrigin;
    }

    public CorsResponseHelper setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
        return this;
    }

    public boolean isAllowOnlyRequestedMethods() {
        return allowOnlyRequestedMethods;
    }

    public CorsResponseHelper setAllowOnlyRequestedMethods(boolean allowOnlyRequestedMethods) {
        this.allowOnlyRequestedMethods = allowOnlyRequestedMethods;
        return this;
    }

    public String getAllowMethods() {
        return allowMethods;
    }

    public CorsResponseHelper setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    public boolean isExposeOnlyRequestedHeader() {
        return exposeOnlyRequestedHeader;
    }

    public CorsResponseHelper setExposeOnlyRequestedHeader(boolean exposeOnlyRequestedHeader) {
        this.exposeOnlyRequestedHeader = exposeOnlyRequestedHeader;
        return this;
    }

    public String getExposeHeaders() {
        return exposeHeaders;
    }

    public CorsResponseHelper setExposeHeaders(String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
        return this;
    }

    public void addCorsResponseHeaders(Resource resource) {
        addCorsResponseHeaders(resource.getRequest(), resource.getResponse());
    }

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
            String accessControlRequestMethods = request.getHeaders().getValues("Access-Control-Allow-Methods");
            if (accessControlRequestMethods != null) {
                headers.set("Access-Control-Allow-Methods", accessControlRequestMethods);
            }
        } else {
            if (allowMethods != null) {
                headers.set("Access-Control-Allow-Methods", allowMethods);
            }
        }

        // Header 'Access-Control-Expose-Headers'
        if (exposeOnlyRequestedHeader) {
            if (request == null) {
                throw new RuntimeException("If exposeOnlyRequestedHeader is true, it requires the request parameter");
            }
            String accessControlRequestHeaders = request.getHeaders().getValues("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                headers.set("Access-Control-Expose-Headers", accessControlRequestHeaders);
            }
        } else {
            if (exposeHeaders != null) {
                headers.set("Access-Control-Expose-Headers", exposeHeaders);
            }
        }
    }
}
