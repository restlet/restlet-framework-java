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

package org.restlet.ext.apispark.internal.utils;

import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

/**
 * RestletChain restletChain = new RestletChain()
 *        .add(createBaseRouter())
 *        .add(createApiGuard())
 *        .add(createApiRouter());
 *
 * Restlet first = restletChain.getFirst();
 * Restlet last = restletChain.getLast();
 *
 * @author Manuel Boillod
 */
public class RestletChain {

    private Restlet first = null;
    private Restlet last = null;

    public RestletChain add(Restlet restlet) {
        if (first == null) {
            first = restlet;
        }
        if (last != null) {
            if (last instanceof Router) {
                Router router = (Router) last;
                router.attachDefault(restlet);
            } else if (last instanceof Filter) {
                Filter filter = (Filter) last;
                filter.setNext(restlet);
            } else {
                throw new IllegalArgumentException("Could not chain any component after a Restlet");
            }
        }
        last = restlet;
        return this;
    }

    public Restlet getFirst() {
        return first;
    }
    public Restlet getLast() {
        return last;
    }
}