package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.example.book.restlet.ch04.sec3.common.AccountResource;
import org.restlet.resource.ClientResource;

/**
 * Client account resource.
 */
public class AccountClientResource extends ClientResource implements
        AccountResource {

    /** Dynamic proxy converting Java calls into HTTP calls. */
    private AccountResource proxy;

    /**
     * Constructor.
     * 
     * @param uri
     *            The target root URI.
     */
    public AccountClientResource(String uri) {
        super(uri);

        // Creates the dynamic proxy
        this.proxy = wrap(AccountResource.class);
    }

    public String represent() {
        return this.proxy.represent();
    }

    public void store(String account) {
        this.proxy.store(account);
    }

    public void remove() {
        this.proxy.remove();
    }
}
