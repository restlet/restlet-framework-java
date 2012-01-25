/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.misc;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/**
 * Display the HTTP accept header sent by the Web browsers.
 * 
 * @author Jerome Louvel
 */
public class HeadersTest {
    public static void main(String[] args) throws Exception {
        final Restlet restlet = new Restlet() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(Request request, Response response) {
                // ------------------------------
                // Getting an HTTP request header
                // ------------------------------
                Series<Header> headers = (Series<Header>) request
                        .getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);

                // The headers list contains all received HTTP headers, in raw
                // format.
                // Below, we simply display the standard "Accept" HTTP header.
                response.setEntity(
                        "Accept header: "
                                + headers.getFirstValue("accept", true),
                        MediaType.TEXT_PLAIN);

                // -----------------------
                // Adding response headers
                // -----------------------
                headers = new Series<Header>(Header.class);

                // Non-standard headers are allowed
                headers.add("X-Test", "Test value");

                // Standard HTTP headers are forbidden. If you happen to add one
                // like the "Location"
                // header below, it will be ignored and a warning message will
                // be displayed in the logs.
                headers.add("Location", "http://www.restlet.org");

                // Setting the additional headers into the shared call's
                // attribute
                response.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                        headers);
            }
        };

        // Create the HTTP server and listen on port 8111
        final Server server = new Server(Protocol.HTTP, 8111, restlet);
        server.start();
    }
}
