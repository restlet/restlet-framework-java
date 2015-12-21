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

import java.util.Map.Entry;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

public class QueryRouter extends Router {
    public QueryRouter(Context context) {
        super(context);
        // Allows you to customize the routing logic.
        setRoutingMode(Router.MODE_CUSTOM);
        // Allows to calculate the score of all routes without taking into
        // account the query part
        setDefaultMatchingQuery(false);
    }

    /**
     * Mix the logic based on URI (except the query part) and the logic based on
     * query parameters.
     */
    @Override
    protected Route getCustom(Request request, Response response) {
        Form form = request.getResourceRef().getQueryAsForm();

        Route result = null;

        float bestScore = 0F;
        float score;

        for (Route route : getRoutes()) {
            TemplateRoute current = (TemplateRoute) route;
            // Logic based on the beginning of the route (i.e. all before the
            // query string)
            score = current.score(request, response);
            if ((score > bestScore)) {

                // Add the logic based on the variables values.
                // Check that all the variables values are correct
                boolean fit = true;
                for (Entry<String, Variable> entry : current.getTemplate()
                        .getVariables().entrySet()) {
                    String formValue = form.getFirstValue(entry.getKey());
                    if (formValue == null
                            || !formValue.equals(entry.getValue()
                                    .getDefaultValue())) {
                        fit = false;
                        break;
                    }
                }
                if (fit) {
                    bestScore = score;
                    result = current;
                }
            }
        }

        return result;
    }
}
