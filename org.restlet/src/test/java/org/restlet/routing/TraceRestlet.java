/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.routing;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

/**
 * Trace Restlet.
 * 
 * @author Jerome Louvel
 */
public class TraceRestlet extends Restlet {
    public TraceRestlet(Context context) {
        super(context);
    }

    /**
     * Handles a uniform call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        final String message = "Hello World!"
                + "\nYour IP address is "
                + request.getClientInfo().getAddress()
                + "\nYour request URI is: "
                + ((request.getResourceRef() == null) ? "?" : request
                        .getResourceRef().toString());
        response.setEntity(message, MediaType.TEXT_PLAIN);
    }

}
