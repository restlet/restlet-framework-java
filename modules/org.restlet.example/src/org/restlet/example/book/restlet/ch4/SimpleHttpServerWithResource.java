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

package org.restlet.example.book.restlet.ch4;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

/**
 *
 */
public class SimpleHttpServerWithResource {
    public static void main(String[] args) {

        final Application application = new Application() {

            @Override
            public synchronized Restlet createRoot() {
                final Router router = new Router(getContext());

                // TODO attach a "hello, world" resource.
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
