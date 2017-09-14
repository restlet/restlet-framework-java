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

package org.restlet.example.book.restlet.ch06.sec2.server;

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
        setOwner("Restlet S.A.S.");
        setAuthor("The Restlet Team");

        // Add a CLAP client connector
        getClients().add(Protocol.CLAP);

        // Adds a HTTP server connector
        Server server = getServers().add(Protocol.HTTP, 8111);
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
