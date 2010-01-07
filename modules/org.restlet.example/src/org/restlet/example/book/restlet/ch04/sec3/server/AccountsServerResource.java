package org.restlet.example.book.restlet.ch04.sec3.server;

import org.restlet.example.book.restlet.ch04.sec3.common.AccountsResource;
import org.restlet.resource.ServerResource;

/**
 * Implementation of the resource containing the list of mail accounts.
 */
public class AccountsServerResource extends ServerResource implements
        AccountsResource {

    public String represent() {
        StringBuilder result = new StringBuilder();

        for (String account : MailServerApplication.getAccounts()) {
            result.append((account == null) ? "" : account).append('\n');
        }

        return result.toString();
    }

    public String add(String account) {
        MailServerApplication.getAccounts().add(account);
        return Integer.toString(MailServerApplication.getAccounts().indexOf(
                account) + 1);
    }
}
