package org.restlet.ext.apispark.internal.utils;

import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

/**
 *
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