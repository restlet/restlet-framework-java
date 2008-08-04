/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch4;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 */
public class SimpleHttpServerWithApplication {
    public static void main(String[] args) {

        final Application application = new Application() {

            @Override
            public synchronized Restlet createRoot() {
                // TODO note that the router is intantiated with the
                // application's context.
                final Router router = new Router(getContext());

                // Creates a Restlet whose response to each request is "Hello,
                // world".
                final Restlet restlet = new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response
                                .setEntity("hello, world", MediaType.TEXT_PLAIN);
                    }
                };
                router.attach(restlet);
                return router;
            }
        };

        final Component component = new Component();
        component.getServers().add(Protocol.HTTP);
        component.getDefaultHost().attach(application);
        try {
            component.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
