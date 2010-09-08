package org.restlet.example.book.restlet.ch03.sect4.sub1;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Setting basic application properties
 */
public class MailServerApplication extends Application {

    /**
     * Launches the application with an HTTP server.
     * 
     * @param args
     *            The arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Server mailServer = new Server(Protocol.HTTP, 8182);
        mailServer.setNext(new MailServerApplication());
        mailServer.start();
    }

    /**
     * Constructor.
     */
    public MailServerApplication() {
        setName("RESTful Mail Server");
        setDescription("Example for 'Restlet in Action' book");
        setOwner("Noelios Technologies");
        setAuthor("The Restlet Team");
    }

    /**
     * Creates a root Restlet to trace requests.
     */
    @Override
    public Restlet createInboundRoot() {
        Blocker blocker = new Blocker(getContext());
        blocker.getBlockedAddresses().add("127.0.0.1");
        blocker.setNext(new Tracer(getContext()));
        return blocker;
    }

}
