/**
 * Copyright 2005-2013 Restlet S.A.S.
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

package org.restlet.ext.osgi;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

/**
 * This class provides an implementation of {@link FilterProvider}. You
 * register this class as an OSGi declarative service. The service declaration
 * should look like:
 * <p>
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="org.example.app.filter">
 *   <implementation class="org.restlet.ext.osgi.BaseFilterProvider"/>
 *   <service>
 *     <provide interface="org.restlet.ext.osgi.FilterProvider"/>
 *   </service>
 * </scr:component>
 * }
 * </pre>
 * </p><p>
 * The referenced services are:
 * <ul>
 *   <li>FilterProvider - optional - policy="static" cardinality="1..1"</li>
 * </ul>
 * </p><p>
 * The provided services are:
 * <ul>
 *   <li>FilterProvider</li>
 * </ul>
 * </p><p>
 * Since filter providers have a reference to filter provider, filters can be
 * chained together.  To get the filters in the desired order, add a service
 * property to a filter, and then place a target filter on the reference
 * declaration.  For example:
 * <pre>
 * <reference bind="bindFilterProvider" cardinality="1..1" target="(type=authFilter)" interface="org.restlet.ext.osgi.FilterProvider" name="FilterProvider" policy="static" unbind="unbindFilterProvider"/>
 * </pre>
 * </p>
 * @author Bryan Hunt
 * 
 */
public abstract class BaseFilterProvider extends BaseRestletProvider implements
        FilterProvider {
    private Filter filter;

    /**
     * Called to construct the actual filter instance.
     * @return the newly constructed filter instance.
     */
    protected abstract Filter createFilter(Context context);

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected Restlet getFilteredRestlet() {
        return filter;
    }

    @Override
    public Restlet getInboundRoot(Context context) {
        if (filter == null)
            filter = createFilter(context);

        Restlet inboundRoot = super.getInboundRoot(context);
        return inboundRoot != null ? inboundRoot : filter;
    }
}
