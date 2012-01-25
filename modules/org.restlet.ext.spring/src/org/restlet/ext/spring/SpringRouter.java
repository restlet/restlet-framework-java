/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.spring;

import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

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
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel
 */
public class SpringRouter extends Router {

    /**
     * Attach a single route.
     * 
     * @param router
     *            The router to attach to.
     * @param path
     *            The attachment URI path.
     * @param route
     *            The route object to attach.
     */
    @SuppressWarnings("unchecked")
    public static void setAttachment(Router router, String path, Object route) {
        Class<?> resourceClass;

        if (route instanceof Restlet) {
            router.attach(path, (Restlet) route);
        } else if (route instanceof Class) {
            router.attach(path, (Class<? extends ServerResource>) route);
        } else if (route instanceof String) {
            try {
                resourceClass = Engine.loadClass((String) route);

                if (org.restlet.resource.ServerResource.class
                        .isAssignableFrom(resourceClass)) {
                    router.attach(path,
                            (Class<? extends ServerResource>) resourceClass);
                } else {
                    router.getLogger()
                            .warning(
                                    "Unknown class found in the mappings. Only subclasses of org.restlet.resource.Resource and ServerResource are allowed.");
                }
            } catch (ClassNotFoundException e) {
                router.getLogger().log(Level.WARNING,
                        "Unable to set the router mappings", e);
            }
        } else {
            router.getLogger()
                    .warning(
                            "Unknown object found in the mappings. Only instances of Restlet and subclasses of org.restlet.resource.Resource and ServerResource are allowed.");
        }
    }

    /**
     * Sets the map of routes to attach.
     * 
     * @param router
     *            The router to attach to.
     * @param routes
     *            The map of routes to attach
     */
    public static void setAttachments(Router router, Map<String, Object> routes) {
        for (String key : routes.keySet()) {
            setAttachment(router, key, routes.get(key));
        }
    }

    /**
     * Constructor.
     */
    public SpringRouter() {
        super();
    }

    /**
     * Constructor with a parent context.
     * 
     * @param context
     *            The parent context.
     */
    public SpringRouter(Context context) {
        super(context);
    }

    /**
     * Constructor with a parent Restlet.
     * 
     * @param parent
     *            The parent Restlet.
     */
    public SpringRouter(Restlet parent) {
        super(parent.getContext());
    }

    /**
     * Sets the map of routes to attach. The map keys are the URI templates and
     * the values can be either Restlet instances, {@link ServerResource}
     * subclasses (as {@link Class} instances or as qualified class names).
     * 
     * @param routes
     *            The map of routes to attach.
     */
    public void setAttachments(Map<String, Object> routes) {
        setAttachments(this, routes);
    }

    /**
     * Sets the default route to attach. The route can be either Restlet
     * instances, {@link ServerResource} subclasses (as {@link Class} instances
     * or as qualified class names).
     * 
     * @param route
     *            The default route to attach.
     */
    public void setDefaultAttachment(Object route) {
        setAttachment(this, "", route);
    }

}
