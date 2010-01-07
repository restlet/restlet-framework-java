package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.Client;
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
     * @param client
     *            The client connector to reuse.
     * @param uri
     *            The target root URI.
     */
    public AccountClientResource(Client client, String uri) {
        super(uri);
        setNext(client);

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
