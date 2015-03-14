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

package org.restlet.example.router;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

public class QueryRouterApplication extends Application {
    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 8182);
        c.getDefaultHost().attach(new QueryRouterApplication());

        c.start();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new QueryRouter(getContext());
        // restlet attached to /path?q=hello
        Restlet restletHello = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("Hello.", MediaType.TEXT_PLAIN);
            }
        };

        // restlet attached to /path?q=bye
        Restlet restletBye = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("Bye", MediaType.TEXT_PLAIN);
            }
        };

        // Defines two routes on the same path, but distinct query parameters.
        TemplateRoute logonRoute = router.attach("/path", restletHello);
        Map<String, Variable> logonVariables = logonRoute.getTemplate()
                .getVariables();
        // Take care of using the same query parameter names
        logonVariables.put("q", new Variable(Variable.TYPE_URI_QUERY, "hello",
                true, true));

        TemplateRoute logoutRoute = router.attach("/path", restletBye);
        Map<String, Variable> logoutVariables = logoutRoute.getTemplate()
                .getVariables();
        // Take care of using the same query parameter names
        logoutVariables.put("q", new Variable(Variable.TYPE_URI_QUERY, "bye",
                true, true));

        return router;
    }
}
