/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

/**
 *
 */
public class TunnelApplication extends Application {

    /**
     * Constructor.
     */
    public TunnelApplication() {
        // Update the default value of the method parameter
        getTunnelService().setMethodParameter("_method");
    }

    @Override
    public Restlet createRoot() {
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                final StringBuilder builder = new StringBuilder();
                builder.append("<html><body>");

                if (Method.GET.equals(request.getMethod())) {
                    // Append a new "method" parameter to the query part of the
                    // resource's reference
                    final Reference ref = new Reference(request
                            .getResourceRef());
                    ref.addQueryParameter("_method", "put");

                    // Build a POST form with the updated action
                    builder.append("<form method=\"POST\"");
                    builder.append("action=\"");
                    builder.append(ref.getIdentifier());
                    builder.append("\">");
                    builder.append("<input type=\"submit\">");
                    builder.append("</form>");
                } else {
                    builder.append("request method is ");
                    builder.append(request.getMethod());
                    builder.append(".");
                }
                builder.append("</body></html>");

                response.setStatus(Status.SUCCESS_OK);
                response.setEntity(new StringRepresentation(builder.toString(),
                        MediaType.TEXT_HTML));
            }
        };

        return restlet;
    }
}
