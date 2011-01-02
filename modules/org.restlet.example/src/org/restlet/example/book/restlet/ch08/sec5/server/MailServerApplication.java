package org.restlet.example.book.restlet.ch08.sec5.server;

import org.restlet.Restlet;
import org.restlet.example.book.restlet.ch07.sec2.server.AccountServerResource;
import org.restlet.example.book.restlet.ch07.sec2.server.AccountsServerResource;
import org.restlet.example.book.restlet.ch08.sec1.sub1.MailServerResource;
import org.restlet.example.book.restlet.ch08.sec1.sub2.CookieAuthenticator;
import org.restlet.example.book.restlet.ch08.sec2.sub1.FeedServerResource;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;
import org.restlet.security.MapVerifier;

/**
 * The reusable mail server application.
 */
public class MailServerApplication extends WadlApplication {

    /**
     * Constructor.
     */
    public MailServerApplication() {
        setName("RESTful Mail Server application");
        setDescription("Example application for 'Restlet in Action' book");
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
        router.attach("/accounts/{accountId}/feeds/{feedId}",
                FeedServerResource.class);
        router.attach("/accounts/{accountId}/mails/{mailId}",
                MailServerResource.class);

        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());

        CookieAuthenticator authenticator = new CookieAuthenticator(
                getContext(), "Cookie Test");
        authenticator.setVerifier(verifier);
        authenticator.setNext(router);
        return authenticator;

    }

}
