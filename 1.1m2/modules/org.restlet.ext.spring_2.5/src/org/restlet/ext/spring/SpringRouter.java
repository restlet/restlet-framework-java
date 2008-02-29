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

package org.restlet.ext.spring;

import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.resource.Resource;

/**
 * Router that is easily configurable with Spring. Here is a usage example:
 * 
 * <pre>
 * &lt;bean class=&quot;org.restlet.ext.spring.SpringRouter&quot;&gt;
 *     &lt;constructor-arg ref=&quot;application&quot; /&gt;
 * 
 *     &lt;property name=&quot;attachments&quot;&gt;
 *         &lt;map&gt;
 *             &lt;entry key=&quot;/users/{user}&quot;                  value=&quot;org.restlet.example.tutorial.UserResource&quot; /&gt;
 *             &lt;entry key=&quot;/users/{user}/orders&quot;           value=&quot;org.restlet.example.tutorial.OrdersResource&quot; /&gt;
 *             &lt;entry key=&quot;/users/{user}/orders/{order}&quot;   value=&quot;org.restlet.example.tutorial.OrderResource&quot; /&gt;
 *         &lt;/map&gt;
 *     &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringRouter extends Router {

    /**
     * Constructor.
     */
    public SpringRouter() {
        super();
    }

    /**
     * Constructor with a parent context.
     */
    public SpringRouter(Context context) {
        super(context);
    }

    /**
     * Constructor with a parent Restlet.
     */
    public SpringRouter(Restlet parent) {
        super(parent.getContext());
    }

    /**
     * Sets the map of routes to attach. The map keys are the URI templates and
     * the values can be either Restlet instances, Resource subclasses (as Class
     * instances or as qualified class names).
     * 
     * @param routes
     *                The map of routes to attach.
     */
    public void setAttachments(Map<String, Object> routes) {
        setAttachments(this, routes);
    }

    /**
     * Sets the map of routes to attach.
     * 
     * @param router
     *                The router to attach to.
     * @param routes
     *                The map of routes to attach
     */
    @SuppressWarnings("unchecked")
    public static void setAttachments(Router router, Map<String, Object> routes) {
        Object value;
        Class resourceClass;

        try {
            for (String key : routes.keySet()) {
                value = routes.get(key);

                if (value instanceof Restlet) {
                    router.attach(key, (Restlet) value);
                } else if (value instanceof Class) {
                    router.attach(key, (Class<? extends Resource>) value);
                } else if (value instanceof String) {
                    resourceClass = Class.forName((String) value);

                    if (Resource.class.isAssignableFrom(resourceClass)) {
                        router.attach(key, resourceClass);
                    } else {
                        router
                                .getLogger()
                                .warning(
                                        "Unknown class found in the mappings. Only subclasses of org.restlet.resource.Resource are allowed.");
                    }
                } else {
                    router
                            .getLogger()
                            .warning(
                                    "Unknown object found in the mappings. Only instances of Restlet and subclasses of org.restlet.resource.Resource are allowed.");
                }
            }
        } catch (ClassNotFoundException e) {
            router.getLogger().log(Level.WARNING,
                    "Unable to set the router mappings", e);
        }
    }

}
