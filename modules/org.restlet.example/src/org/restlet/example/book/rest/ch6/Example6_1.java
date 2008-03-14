/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.example.book.rest.ch6;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;

/**
 * Sample map client to create a user account
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Example6_1 {
    public void makeUser(String user, String password) {
        // Create the input form
        Form input = new Form();
        input.add("password", password);

        // Create the target URI, encoding the user name
        String uri = "https://maps.example.com/user/" + Reference.encode(user);

        // Invoke the web service
        new Client(Protocol.HTTP).put(uri, input.getWebRepresentation());
    }
}
