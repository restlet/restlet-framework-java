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

package org.restlet.example.book.restlet.ch07.sec1.sub4;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;

/**
 * The mail server application using a custom status service.
 */
public class MailServerApplication extends WadlApplication {

    /**
     * Launches the application with an HTTP server.
     * 
     * @param args
     *            The arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Component mailServer = new Component();
        mailServer.getClients().add(Protocol.CLAP);
        mailServer.getServers().add(Protocol.HTTP, 8111);
        mailServer.getDefaultHost().attach(new MailServerApplication());
        mailServer.start();
    }

    /**
     * Constructor.
     */
    public MailServerApplication() {
        setName("RESTful Mail Server application");
        setDescription("Example application for 'Restlet in Action' book");
        setOwner("Restlet S.A.S.");
        setAuthor("The Restlet Team");

        // Configure the status service
        setStatusService(new MailStatusService());
    }

    /**
     * Creates a root Router to dispatch call to server resources.
     */
    @Override
    public Restlet createInboundRoot() {
        return new Router(getContext());
    }

}
