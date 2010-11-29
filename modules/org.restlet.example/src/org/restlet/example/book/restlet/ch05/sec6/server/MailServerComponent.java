package org.restlet.example.book.restlet.ch05.sec6.server;

import org.restlet.Component;
import org.restlet.Server;
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
        Server server = getServers().add(Protocol.HTTP, 8082);
        server.getContext().getParameters().set("tracing", "true");

        // Configure the default virtual host
        // getDefaultHost().setHostDomain("www.rmep.com|www.rmep.net|www.rmep.org");
        // getDefaultHost().setServerAddress("1.2.3.10|1.2.3.20");
        // getDefaultHost().setServerPort("80");

        // Attach the application to the default virtual host
        getDefaultHost().attachDefault(new MailServerApplication());

        // Configure the log service
        getLogService().setLoggerName("MailServer.AccessLog");
        getLogService()
                .setLogPropertiesRef(
                        "clap://system/org/restlet/example/book/restlet/ch04/sec3/server/log.properties");
    }
}
