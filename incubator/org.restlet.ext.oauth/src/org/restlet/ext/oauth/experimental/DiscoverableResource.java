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

package org.restlet.ext.oauth.experimental;

import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.security.Role;

/**
 * EXPERIMENTAL, and not part of the OAuth specification Implementation might
 * change in future releases.
 * 
 * Marker interface that can help developer discover OAuth protected resources
 * and how to proceed when access id denied.
 * 
 * There is an old XRD based OAuth 1.0 discovery but is not updated.
 * 
 * @author Kristoffer Gronowski
 */
public interface DiscoverableResource{
    
    public String getOwner(Reference uri);
    public List <Role> getRoles(Reference uri, Method method);
    
}