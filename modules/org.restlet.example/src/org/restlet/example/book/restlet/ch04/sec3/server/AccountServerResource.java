package org.restlet.example.book.restlet.ch04.sec3.server;

import org.restlet.example.book.restlet.ch04.sec3.common.AccountResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Implementation of a mail account resource.
 */
public class AccountServerResource extends ServerResource implements
        AccountResource {

    /** The account identifier. */
    private int accountId;

    /**
     * Retrieve the account identifier based on the URI path variable
     * "accountId" declared in the URI template attached to the application
     * router.
     */
    @Override
    protected void doInit() throws ResourceException {
        this.accountId = Integer.parseInt((String) getRequestAttributes().get(
                "accountId"));
    }

    public String represent() {
        return MailServerApplication.getAccounts().get(this.accountId - 1);
    }

    public void store(String account) {
        MailServerApplication.getAccounts().set(this.accountId - 1, account);
    }

    public void remove() {
        MailServerApplication.getAccounts().remove(this.accountId - 1);
    }
}
