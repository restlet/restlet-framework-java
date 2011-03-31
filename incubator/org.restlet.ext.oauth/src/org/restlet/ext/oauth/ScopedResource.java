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

package org.restlet.ext.oauth;

import org.restlet.data.Reference;

/**
 * Interface class that a Restlet protected resource should implement. By
 * default the ValidationResource only check that the access token is valid If
 * the resource implement this interface then also required scopes and the
 * required user to execute the resource is matched
 * 
 * @see org.restlet.ext.oauth.ValidationServerResource
 * 
 *      Defines a scoped resource. Implementors implement this interface in
 *      conjunction with their ServerResource. A RemoteAuthorizer will
 *      automatically check if the ServerResource is of type ScopedResource and
 *      if so use the scope and owner information (if provided). The following
 *      code snippets show how it can be used
 * 
 *      <pre>
 * {@code
 * class MyResource extends ServerResource implements ScopedResource{
 *   public String[] getScope(Reference uri) {
 *     return "myScope";
 *   }
 * 
 *   public String getOwner(Reference uri) {
 *     return "owner";
 *   }
 * }
 * ...
 * public Restlet createInboundRoot(){
 *   ...
 *   RemoteAuthorizer auth = new RemoteAuthorizer("validateURL","authorizeURL");
 *   auth.setNext(MyResource.class);
 *   router.attach(resourcePath, auth);
 *   ...
 * }
 * }
 * </pre>
 * 
 *      The above example will set up a scoped resource MyResource that requires
 *      that a client has been granted access to scope "myScope" from owner
 *      "owner"
 * 
 * @author Kristoffer Gronowski
 */
public interface ScopedResource {

    /**
     * Gets the URI that the requestor used to access this resource. An
     * overriding class can inspect the URI and determine what scope would be
     * needed to access this particular resource.
     * 
     * The criteria for determining the scope are totally up to the implementor
     * and can by dynamic like time of day
     * 
     * @param uri
     *            URI used to access this resource
     * @return Array of string representing all the mandatory scopes in order to
     *         proceed.
     */
    public String[] getScope(Reference uri);

    /**
     * Gets the URI that the requestor used to access this resource. An
     * overriding class can inspect the URI and determine what user if any is
     * required for a particular resource.
     * 
     * This function is particularly interesting on insert and update operation
     * since most likely it is only the owner that should be able to update the
     * data set.
     * 
     * @param uri
     *            URI used to access this resource
     * @return String with the ID of the user
     */
    public String getOwner(Reference uri);

}
