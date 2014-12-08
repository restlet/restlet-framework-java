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

package org.restlet.ext.apispark.internal.firewall.rule;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

/**
 * A {@link FirewallRule} has the semantic of a {@link Filter}. It is able to
 * check a request and to update the response.
 * 
 * @author Guillaume Blondeau
 */
public abstract class FirewallRule extends Filter {

    /**
     * Updates the response, if necessary. It does nothing by default.
     * 
     * @param request
     *            The request.
     * @param response
     *            the response.
     */
    public void afterHandle(Request request, Response response) {

    }

    /**
     * Checks the given request. It returns {@link Filter#CONTINUE} by default;
     * 
     * @param request
     *            The request to check.
     * @param response
     *            The response to check.
     * @return {@link Filter#CONTINUE} if the rule is successfully applied,
     *         {@link Filter#STOP} to block it.
     */
    public int beforeHandle(Request request, Response response) {
        return Filter.CONTINUE;
    }

}
