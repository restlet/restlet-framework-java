/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch4;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 */
public class SimplePostClient {
    // 
    // review=ljjll

    public static void main(String[] args) {
        // Instantiates a client according to a protocol
        final Client client = new Client(Protocol.HTTP);
        // Instantiates a request with a method and the resource's URI
        final Request request = new Request(Method.POST,
                "http://www.comp.leeds.ac.uk/cgi-bin/Perl/environment-example");
        final Form form = new Form();
        form.set("review", "ljjll", false);
        request.setEntity(form.getWebRepresentation());

        // Sends the request and gets the response
        final Response response = client.handle(request);

        // Prints the status of the response
        System.out.println(response.getStatus());

        // Writes the response's entity content, if available
        if (response.isEntityAvailable()) {
            try {
                response.getEntity().write(System.out);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
