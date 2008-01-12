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
 * Virtual host that is easily configurable with Spring.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)</a>
 */
public class SpringHost extends VirtualHost {

    /**
     * Sets the modifiable list of routes.
     * 
     * @param routes
     *                The modifiable list of routes.
     */
    public void setRoutes(Map<String, Object> routes) {
        SpringRouter.setAttachments(this, routes);
    }

}
