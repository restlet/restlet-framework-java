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

package org.restlet.example.book.rest.ch7;

import java.io.File;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.util.Variable;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;

/**
 * The main Web application.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Application extends org.restlet.Application {

    public static void main(String... args) throws Exception {
        // Create a component with an HTTP server connector
        Component comp = new Component();
        comp.getServers().add(Protocol.HTTP, 3000);

        // Attach the application to the default host and start it
        comp.getDefaultHost().attach("/v1", new Application());
        comp.start();
    }

    private ObjectContainer container;

    public Application() {
        /** Open and keep the db4o object container. */
        Configuration config = Db4o.configure();
        config.updateDepth(2);
        this.container = Db4o.openFile(System.getProperty("user.home")
                + File.separator + "restbook.dbo");
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        // Add a route for user resources
        router.attach("/users/{username}", UserResource.class);

        // Add a route for user's bookmarks resources
        router.attach("/users/{username}/bookmarks", BookmarksResource.class);

        // Add a route for bookmark resources
        Route uriRoute = router.attach("/users/{username}/bookmarks/{URI}",
                BookmarkResource.class);
        uriRoute.getTemplate().getVariables().put("URI",
                new Variable(Variable.TYPE_URI_ALL));

        return router;
    }

    /**
     * Returns the database container.
     * 
     * @return the database container.
     */
    public ObjectContainer getContainer() {
        return this.container;
    }

}
