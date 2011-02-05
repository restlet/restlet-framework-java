/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.spring;

import java.util.Map;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.resource.ServerResource;
import org.restlet.routing.VirtualHost;

/**
 * Virtual host that is easily configurable with Spring. Here is a usage
 * example:
 * 
 * <pre>
 *     &lt;bean id=&quot;virtualHost&quot; class=&quot;org.restlet.ext.spring.SpringHost&quot;&gt;
 *         &lt;constructor-arg ref=&quot;component&quot; /&gt;
 *         &lt;property name=&quot;hostDomain&quot;
 *                 value=&quot;mydomain.com|www.mydomain.com&quot; /&gt;
 *         &lt;property name=&quot;attachments&quot;&gt;
 *             &lt;map&gt;
 *                 &lt;entry key=&quot;/&quot;&gt;
 *                     &lt;ref bean=&quot;application&quot; /&gt;
 *                 &lt;/entry&gt;
 *             &lt;/map&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel
 */
public class SpringHost extends VirtualHost {

    /**
     * Constructor.
     * 
     * @param component
     *            The parent component.
     */
    public SpringHost(Component component) {
        super(component.getContext());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     */
    public SpringHost(Context context) {
        super(context);
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
        SpringRouter.setAttachments(this, routes);
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
        SpringRouter.setAttachment(this, "", route);
    }

}
