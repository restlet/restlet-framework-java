package org.restlet.example.book.restlet.ch07.sec2.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.example.book.restlet.ch03.sect5.sub5.common.AccountsResource;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Implementation of the resource containing the list of mail accounts.
 */
public class AccountsServerResource extends WadlServerResource implements
        AccountsResource {

    /** Static list of accounts stored in memory. */
    private static final List<String> accounts = new CopyOnWriteArrayList<String>();

    @Override
    protected void doInit() throws ResourceException {
        setName("Mail accounts resource");
        setDescription("The resource containing the list of mail accounts");
    }

    /**
     * Returns the static list of accounts stored in memory.
     * 
     * @return The static list of accounts.
     */
    public static List<String> getAccounts() {
        return accounts;
    }

    public String represent() {
        StringBuilder result = new StringBuilder();

        for (String account : getAccounts()) {
            result.append((account == null) ? "" : account).append('\n');
        }

        return result.toString();
    }

    public String add(String account) {
        getAccounts().add(account);
        return Integer.toString(getAccounts().indexOf(account) + 1);
    }
}
