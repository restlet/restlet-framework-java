package org.restlet.example.book.restlet.ch03.sect3.sub2;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;

/**
 * Setting basic application properties.
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
        return new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                String entity = "Method       : " + request.getMethod()
                        + "\nResource URI : " + request.getResourceRef()
                        + "\nIP address   : "
                        + request.getClientInfo().getAddress()
                        + "\nAgent name   : "
                        + request.getClientInfo().getAgentName()
                        + "\nAgent version: "
                        + request.getClientInfo().getAgentVersion();
                response.setEntity(entity, MediaType.TEXT_PLAIN);
            }
        };
    }
}
