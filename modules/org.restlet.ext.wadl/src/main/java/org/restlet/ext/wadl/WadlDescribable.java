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

package org.restlet.ext.wadl;

import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;

/**
 * Interface that any Restlet can implement in order to provide their own WADL
 * documentation. This is especially useful for subclasses of {@link Directory}
 * or other resource finders when the WADL introspection can't reach
 * {@link ServerResource} or better {@link WadlServerResource} instances.
 * 
 * @author Thierry Boileau
 */
public interface WadlDescribable {

    /**
     * Returns a full documented {@link ResourceInfo} instance.
     * 
     * @param applicationInfo
     *            The parent WADL application descriptor.
     * 
     * @return A full documented {@link ResourceInfo} instance.
     */
    ResourceInfo getResourceInfo(ApplicationInfo applicationInfo);

}
