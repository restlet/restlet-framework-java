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

package org.restlet.example.book.restlet.ch07.sec3.sub1;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource that simply redirects to the port 8111.
 */
public class OldServerResource extends ServerResource {

    @Get
    public String redirect() {
        // Sets the response status to 301 (Moved Permanently)
        redirectPermanent("http://localhost:8111/");

        System.out.println("Redirecting client to new location...");

        // Add explanation message entity
        return "Resource moved... \n";
    }
}
