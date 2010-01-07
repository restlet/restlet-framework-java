package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.Client;
import org.restlet.example.book.restlet.ch04.sec3.common.RootResource;
import org.restlet.resource.ClientResource;

/**
 * Client root resource.
 */
public class RootClientResource extends ClientResource implements RootResource {

    /** Dynamic proxy converting Java calls into HTTP calls. */
    private RootResource proxy;

    /**
     * Constructor.
     * 
     * @param client
     *            The client connector to reuse.
     * @param uri
     *            The target root URI.
     */
    public RootClientResource(Client client, String uri) {
        super(uri);
        setNext(client);

        // Creates the dynamic proxy
        this.proxy = wrap(RootResource.class);
    }

    public String represent() {
        // Delegates the call to the dynamic proxy
        return this.proxy.represent();
    }

}
