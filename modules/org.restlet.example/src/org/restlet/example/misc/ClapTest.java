/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.CLAP);

        Application application = new Application(component.getContext()) {

            @Override
            public Restlet createRoot() {
                getConnectorService().getClientProtocols().add(Protocol.CLAP);
                getConnectorService().getServerProtocols().add(Protocol.HTTP);

                Directory directory = new Directory(getContext(),
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
