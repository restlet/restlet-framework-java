package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch04.sec3.common.AccountResource;
import org.restlet.example.book.restlet.ch04.sec3.common.AccountsResource;
import org.restlet.example.book.restlet.ch04.sec3.common.RootResource;

/**
 * Mail client.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        System.out.println("\n1) Set-up the client connector\n");
        Client client = new Client(Protocol.HTTP);
        client.start();

        System.out.println("\n2) Display the root resource\n");
        RootResource mailRoot = new RootClientResource(client,
                "http://localhost:8182/");
        System.out.println(mailRoot.represent());

        System.out.println("\n3) Display the initial list of accounts\n");
        AccountsResource mailAccounts = new AccountsClientResource(client,
                "http://localhost:8182/accounts/");
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
        AccountResource mailAccount = new AccountClientResource(client,
                "http://localhost:8182/accounts/2");
        System.out.println(mailAccount.represent());

        System.out
                .println("\n7) Update the individual account and display it again\n");
        mailAccount.store("Roy T. Fielding");
        System.out.println(mailAccount.represent());

        System.out
                .println("\n8) Delete the first account and display the list again\n");
        mailAccount = new AccountClientResource(client,
                "http://localhost:8182/accounts/1");
        mailAccount.remove();
        System.out.println(mailAccounts.represent());
    }
}
