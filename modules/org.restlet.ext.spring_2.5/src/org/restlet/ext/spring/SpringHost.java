/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.spring;

import java.util.Map;

import org.restlet.VirtualHost;

/**
 * Virtual host that is easily configurable with Spring. Here is a usage
 * example:
 * 
 * <pre>
 *     &lt;bean id=&quot;virtualHost&quot; class=&quot;org.restlet.ext.spring.SpringHost&quot;&gt;
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
 * @see <a href="http://www.springframework.org/">Spring home page< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringHost extends VirtualHost {

    /**
     * Sets the map of routes to attach. The map keys are the URI templates and
     * the values can be either Restlet instances, Resource subclasses (as Class
     * instances or as qualified class names).
     * 
     * @param routes
     *            The map of routes to attach.
     */
    public void setAttachments(Map<String, Object> routes) {
        SpringRouter.setAttachments(this, routes);
    }

}
