/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.example.misc;

import org.restlet.Client;
import org.restlet.data.CharacterSet;
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
        Request request = new Request();

        // Identify ourselves.
        request.setReferrerRef("http://www.foo.com/");

        // Target resource.
        request.setResourceRef("http://127.0.0.1:9876/test");

        // Action: Update
        request.setMethod(Method.PUT);

        Form form = new Form();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");
        request.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));

        // Prepare HTTP client connector.
        Client client = new Client(Protocol.HTTP);

        // Make the call.
        Response response = client.handle(request);

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
