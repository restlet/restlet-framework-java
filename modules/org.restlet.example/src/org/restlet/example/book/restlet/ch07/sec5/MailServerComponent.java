/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch07.sec5;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch07.sec5.webapi.server.MailApiApplication;
import org.restlet.example.book.restlet.ch07.sec5.website.MailSiteApplication;

/**
 * RESTful component containing the mail API and mail site applications.
 */
public class MailServerComponent extends Component {

    /**
     * Launches the mail server component.
     * 
     * @param args
     *            The arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new MailServerComponent().start();
    }

    /**
     * Constructor.
     * 
     * @throws Exception
     */
    public MailServerComponent() throws Exception {
        // Set basic properties
        setName("RESTful Mail Server component");
        setDescription("Example for 'Restlet in Action' book");
        setOwner("Restlet S.A.S.");
        setAuthor("The Restlet Team");

        // Add client connectors
        getClients().add(Protocol.CLAP);

        // Adds server connectors
        getServers().add(Protocol.HTTP, 8111);

        // Attach the applications
        getDefaultHost().attach("/site", new MailSiteApplication());
        getInternalRouter().attach("/api", new MailApiApplication());
    }
}
