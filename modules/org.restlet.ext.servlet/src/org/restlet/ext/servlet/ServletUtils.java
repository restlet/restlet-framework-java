/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.http.Call;
import org.restlet.engine.http.HttpRequest;
import org.restlet.engine.http.HttpResponse;
import org.restlet.ext.servlet.internal.ServletCall;

/**
 * Servlet related utilities.
 * 
 * @author Jerome Louvel
 */
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
