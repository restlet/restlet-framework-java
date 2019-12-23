/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.osgi;

import org.restlet.routing.Filter;

/**
 * This is an OSGi service interface for registering Restlet filters with a
 * router or a resource. Users are expected to register an instance as an OSGi
 * service. It is recommended that you extend the {@link BaseFilterProvider}
 * implementation. You may provide your own implementation of
 * {@link FilterProvider} if you need complete control.
 * 
 * @author Bryan Hunt
 */
public interface FilterProvider extends RestletProvider {
    /**
     * 
     * @return the filter instance
     */
    Filter getFilter();
}
