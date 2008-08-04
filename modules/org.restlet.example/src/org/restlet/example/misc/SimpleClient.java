/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.misc;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Simple HTTP client calling the simple server.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SimpleClient {
    public static void main(String[] args) throws Exception {
        // Prepare the REST call.
        final Request request = new Request();

        // Identify ourselves.
        request.setReferrerRef("http://www.foo.com/");

        // Target resource.
        request.setResourceRef("http://127.0.0.1:9876/test");

        // Action: Update
        request.setMethod(Method.PUT);

        final Form form = new Form();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");
        request.setEntity(form.getWebRepresentation());

        // Prepare HTTP client connector.
        final Client client = new Client(Protocol.HTTP);

        // Make the call.
        final Response response = client.handle(request);

        if (response.getStatus().isSuccess()) {
            // Output the response entity on the JVM console
            response.getEntity().write(System.out);
            System.out.println("client: success!");
        } else {
            System.out.println("client: failure!");
            System.out.println(response.getStatus().getDescription());
        }
    }
}
