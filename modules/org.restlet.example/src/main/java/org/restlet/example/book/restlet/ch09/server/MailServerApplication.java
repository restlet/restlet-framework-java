/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.example.book.restlet.ch09.server;

import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;

/**
 * The reusable mail server application.
 */
public class MailServerApplication extends WadlApplication {

    /**
     * Constructor.
     */
    public MailServerApplication() {
        setName("RESTful Mail API application");
        setDescription("Example API for 'Restlet in Action' book");
        setOwner("QlikTech International AB");
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
        router.attach("/accounts/{accountId}/mails/", MailsServerResource.class);
        router.attach("/accounts/{accountId}/mails/{mailId}",
                MailServerResource.class);
        router.attach("/accounts/{accountId}/contacts/",
                ContactsServerResource.class);
        router.attach("/accounts/{accountId}/contacts/{contactId}",
                ContactServerResource.class);
        return router;
    }

}
