package org.restlet.example.book.restlet.ch08.sec5.server;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch08.sec5.server.webapi.MailApiApplication;
import org.restlet.example.book.restlet.ch08.sec5.server.website.MailSiteApplication;

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
        setOwner("Noelios Technologies");
        setAuthor("The Restlet Team");

        // Add client connectors
        getClients().add(Protocol.CLAP);

        // Adds server connectors
        getServers().add(Protocol.HTTP, 8111);

        // Attach the applications to the default virtual host
        getDefaultHost().attach("/site", new MailSiteApplication());
        getDefaultHost().attach("/api", new MailApiApplication());
    }
}
