/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.tutorial;

import org.restlet.resource.ClientResource;

/**
 * Retrieving the content of a Web page (detailed).
 * 
 * @author Jerome Louvel
 */
public class Part02b_RetrieveContent {
    public static void main(String[] args) throws Exception {
        // Create the client resource
        ClientResource resource = new ClientResource("http://restlet.org");

        // Customize the referrer property
        resource.setReferrerRef("http://www.mysite.org");

        // Write the response entity on the console
        resource.get().write(System.out);
    }

}
