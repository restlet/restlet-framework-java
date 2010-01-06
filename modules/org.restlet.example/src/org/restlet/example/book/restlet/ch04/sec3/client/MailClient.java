package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.example.book.restlet.ch04.sec3.common.AccountResource;
import org.restlet.example.book.restlet.ch04.sec3.common.AccountsResource;
import org.restlet.example.book.restlet.ch04.sec3.common.RootResource;

/**
 * Mail client.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        // 1) Display the root resource
        RootResource mailRoot = new RootClientResource("http://localhost:8182/");
        System.out.println(mailRoot.represent());

        // 2) Display the initial list of accounts
        AccountsResource mailAccounts = new AccountsClientResource(
                "http://localhost:8182/accounts/");
        System.out.println(mailAccounts.represent());

        // 3) Adds new accounts
        mailAccounts.add("Tim Berners-Lee");
        mailAccounts.add("Roy Fielding");
        mailAccounts.add("Mark Baker");

        // 4) Display the updated list of accounts
        System.out.println(mailAccounts.represent());

        // 5) Display an individual account
        AccountResource mailAccount = new AccountClientResource(
                "http://localhost:8182/accounts/2");
        System.out.println(mailAccount.represent());

        // 6) Update the individual account and display it again
        mailAccount.store("Roy T. Fielding");
        System.out.println(mailAccount.represent());

        // 7) Delete the first account and display the list again
        mailAccount = new AccountClientResource(
                "http://localhost:8182/accounts/1");
        mailAccount.remove();
        System.out.println(mailAccounts.represent());
    }

}
