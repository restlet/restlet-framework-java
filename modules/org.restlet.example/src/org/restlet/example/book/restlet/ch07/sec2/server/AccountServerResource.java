package org.restlet.example.book.restlet.ch07.sec2.server;

import org.restlet.example.book.restlet.ch03.sect5.sub5.common.AccountResource;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Implementation of a mail account resource.
 */
public class AccountServerResource extends WadlServerResource implements
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

        String accountIdAttribute = (String) getRequestAttributes().get(
                "accountId");

        if (accountIdAttribute != null) {
            this.accountId = Integer.parseInt(accountIdAttribute);
            setName("Resource for mail account \"" + this.accountId + "\"");
            setDescription("The resource describing the mail account number \""
                    + this.accountId + "\"");
        } else {
            setName("Mail account resource");
            setDescription("The resource describing a mail account");
        }
    }

    public String represent() {
        return AccountsServerResource.getAccounts().get(this.accountId - 1);
    }

    public void store(String account) {
        AccountsServerResource.getAccounts().set(this.accountId - 1, account);
    }

    public void remove() {
        AccountsServerResource.getAccounts().remove(this.accountId - 1);
    }
}
