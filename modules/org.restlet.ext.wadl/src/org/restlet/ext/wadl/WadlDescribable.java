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
    public ResourceInfo getResourceInfo(ApplicationInfo applicationInfo);

}
