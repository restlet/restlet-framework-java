/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.adapter.Call;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.HttpResponse;
import org.restlet.ext.servlet.internal.ServletCall;

/**
 * Servlet related utilities.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public final class ServletUtils {

    /**
     * Returns the Servlet request that was used to generate the given Restlet
     * request.
     * 
     * @param request
     *            The Restlet request.
     * @return The Servlet request or null.
     */
    public static HttpServletRequest getRequest(Request request) {
        HttpServletRequest result = null;

        if (request instanceof HttpRequest) {
            Call call = ((HttpRequest) request).getHttpCall();

            if (call instanceof ServletCall) {
                result = ((ServletCall) call).getRequest();
            }
        }

        return result;
    }

    /**
     * Returns the Servlet response that was used to generate the given Restlet
     * response.
     * 
     * @param response
     *            The Restlet response.
     * @return The Servlet request or null.
     */
    public static HttpServletResponse getResponse(Response response) {
        HttpServletResponse result = null;

        if (response instanceof HttpResponse) {
            Call call = ((HttpResponse) response).getHttpCall();

            if (call instanceof ServletCall) {
                result = ((ServletCall) call).getResponse();
            }
        }

        return result;
    }

}
