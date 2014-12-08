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

package org.restlet.example.misc;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;

/**
 * HTTP server exposing a Directory of resources based on a local CLAP client
 * (ClassLoader Access Protocol).
 * 
 * @author Jerome Louvel
 */
public class ClapTest {
    public static void main(String[] args) throws Exception {

        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.CLAP);

        final Application application = new Application() {

            @Override
            public Restlet createInboundRoot() {
                getConnectorService().getClientProtocols().add(Protocol.CLAP);
                getConnectorService().getServerProtocols().add(Protocol.HTTP);

                final Directory directory = new Directory(getContext(),
                        "clap://class");
                directory.setListingAllowed(true);
                directory.setDeeplyAccessible(true);

                return directory;
            }
        };

        component.getDefaultHost().attach(application);
        component.start();
    }
}
