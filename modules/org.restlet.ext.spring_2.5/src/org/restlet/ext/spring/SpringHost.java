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
