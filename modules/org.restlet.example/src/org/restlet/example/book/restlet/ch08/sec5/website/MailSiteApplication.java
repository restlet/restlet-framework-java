/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch08.sec5.website;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.MapVerifier;

/**
 * The reusable mail server application.
 */
public class MailSiteApplication extends Application {

    /**
     * Constructor.
     */
    public MailSiteApplication() {
        setName("RESTful Mail Site application");
        setDescription("Example Site for 'Restlet in Action' book");
        setOwner("Noelios Technologies");
        setAuthor("The Restlet Team");

        // Configure the status service
        setStatusService(new MailStatusService());
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

        // Serve static files (images, etc.)
        String rootUri = "file:///" + System.getProperty("java.io.tmpdir");
        Directory directory = new Directory(getContext(), rootUri);
        directory.setListingAllowed(true);
        router.attach("/static", directory);

        // Create a Redirector to Google search service
        String target = "http://www.google.com/search?q=site:mysite.org+{keywords}";
        Redirector redirector = new Redirector(getContext(), target,
                Redirector.MODE_CLIENT_TEMPORARY);

        // While routing requests to the redirector, extract the "kwd" query
        // parameter. For instance :
        // http://localhost:8111/search?kwd=myKeyword1+myKeyword2
        // will be routed to
        // http://www.google.com/search?q=site:mysite.org+myKeyword1%20myKeyword2
        Extractor extractor = new Extractor(getContext(), redirector);
        extractor.extractFromQuery("keywords", "kwd", true);

        // Attach the extractor to the router
        router.attach("/search", extractor);

        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());

        CookieAuthenticator authenticator = new CookieAuthenticator(
                getContext(), "Cookie Test");
        authenticator.setVerifier(verifier);
        authenticator.setNext(router);
        return authenticator;

    }

}
