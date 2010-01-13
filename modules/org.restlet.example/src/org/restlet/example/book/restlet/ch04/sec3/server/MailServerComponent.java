package org.restlet.example.book.restlet.ch04.sec3.server;

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
     */
    public MailServerComponent() {
        setName("RESTful Mail Server component");
        setDescription("Example for 'Restlet in Action' book");
        setOwner("Noelios Technologies");
        setAuthor("The Restlet Team");

        // Adds a HTTP server connector
        getServers().add(Protocol.HTTP, 8182);

        // Attach the application to the default (local) host
        getDefaultHost().attachDefault(new MailServerApplication());
    }

}
