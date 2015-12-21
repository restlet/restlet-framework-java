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

package org.restlet.example.book.restlet.ch06.sec2.client;

import org.restlet.resource.ClientResource;

/**
 * Mail client.
 */
public class MailClient {

    /**
     * Mail client interacting with the RESTful mail server.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n1) Set-up the service client resource\n");
        ClientResource service = new ClientResource("http://localhost:8111");

        System.out.println("\n2) Describe the application\n");
        System.out.println(service.options().getText());

        System.out.println("\n3) Describe the accounts resource\n");
        System.out.println(service.getChild("/accounts/").options().getText());
    }

}
