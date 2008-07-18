/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.example.misc;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * HTTP server exposing a Directory of resources based on a local CLAP client
 * (ClassLoader Access Protocol).
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClapTest {
    public static void main(String[] args) throws Exception {

        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.CLAP);

        final Application application = new Application(component.getContext()) {

            @Override
            public Restlet createRoot() {
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
