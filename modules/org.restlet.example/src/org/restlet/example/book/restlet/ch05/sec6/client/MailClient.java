/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.example.book.restlet.ch05.sec6.client;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch03.sect5.sub5.common.AccountResource;
import org.restlet.example.book.restlet.ch03.sect5.sub5.common.AccountsResource;
import org.restlet.example.book.restlet.ch03.sect5.sub5.common.RootResource;
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
        Client client = new Client(new Context(), Protocol.HTTP);
        ClientResource service = new ClientResource("http://localhost:8111");
        service.setNext(client);

        System.out.println("\n2) Display the root resource\n");
        RootResource mailRoot = service.getChild("/", RootResource.class);
        System.out.println(mailRoot.represent());

        System.out.println("\n3) Display the initial list of accounts\n");
        AccountsResource mailAccounts = service.getChild("/accounts/",
                AccountsResource.class);
        String list = mailAccounts.represent();
        System.out.println(list == null ? "<empty>\n" : list);

        System.out.println("4) Adds new accounts\n");
        mailAccounts.add("Tim Berners-Lee");
        mailAccounts.add("Roy Fielding");
        mailAccounts.add("Mark Baker");
        System.out.println("Three accounts added !");

        System.out.println("\n5) Display the updated list of accounts\n");
        System.out.println(mailAccounts.represent());

        System.out.println("6) Display the second account\n");
        AccountResource mailAccount = service.getChild("/accounts/2",
                AccountResource.class);
        System.out.println(mailAccount.represent());

        System.out
                .println("\n7) Update the individual account and display it again\n");
        mailAccount.store("Roy T. Fielding");
        System.out.println(mailAccount.represent());

        System.out
                .println("\n8) Delete the first account and display the list again\n");
        mailAccount = service.getChild("/accounts/1", AccountResource.class);
        mailAccount.remove();
        System.out.println(mailAccounts.represent());
    }

}
