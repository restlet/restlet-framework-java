package org.restlet.example.book.restlet.ch08.sec5.server.webapi;

import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;

/**
 * The reusable mail server application.
 */
public class MailApiApplication extends WadlApplication {

    /**
     * Constructor.
     */
    public MailApiApplication() {
        setName("RESTful Mail API application");
        setDescription("Example API for 'Restlet in Action' book");
        setOwner("Noelios Technologies");
        setAuthor("The Restlet Team");
    }

    /**
     * Creates a root Router to dispatch call to server resources.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/", RootServerResource.class);
        router.attach("/accounts/", AccountsServerResource.class);
        router.attach("/accounts/{accountId}", AccountServerResource.class);
        router.attach("/accounts/{accountId}/mails/{mailId}",
                MailServerResource.class);
        return router;
    }

}
