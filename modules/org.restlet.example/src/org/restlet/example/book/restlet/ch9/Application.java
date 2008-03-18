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

package org.restlet.example.book.restlet.ch9;

import java.io.File;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch9.resources.MailBoxResource;
import org.restlet.example.book.restlet.ch9.resources.MailBoxesResource;
import org.restlet.example.book.restlet.ch9.resources.MailRootResource;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;

/**
 * The main Web application.
 */
public class Application extends org.restlet.Application {

    public static void main(String... args) throws Exception {
        // Create a component with an HTTP server connector
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8585);

        // Attach the application to the default host and start it
        component.getDefaultHost().attach("/rmep", new Application());
        component.start();
    }

    /** Db4o object container. */
    private ObjectContainer db4oContainer;

    public Application() {
        /** Open and keep the db4o object container. */
        Configuration config = Db4o.configure();
        config.updateDepth(2);
        this.db4oContainer = Db4o.openFile(System.getProperty("user.home")
                + File.separator + "rmep.dbo");
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        // Add a route for the MailRoot resource
        router.attachDefault(MailRootResource.class);

        // Add a route for the MailBoxes resource
        router.attach("/mailboxes", MailBoxesResource.class);

        // Add a route for a MailBox resource
        router.attach("/mailboxes/{mailboxId}", MailBoxResource.class);

        return router;
    }

    /**
     * Returns the database container.
     * 
     * @return the database container.
     */
    public ObjectContainer getDb4oContainer() {
        return this.db4oContainer;
    }

}
