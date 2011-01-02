package org.restlet.example.book.restlet.ch08.sec5.server;

import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * RESTful component containing the mail server application.
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

        // Add a CLAP client connector
        getClients().add(Protocol.CLAP);

        // Adds a HTTP server connector
        getServers().add(Protocol.HTTP, 8111);

        // Attach the application to the default virtual host
        getDefaultHost().attachDefault(new MailServerApplication());
    }
}
