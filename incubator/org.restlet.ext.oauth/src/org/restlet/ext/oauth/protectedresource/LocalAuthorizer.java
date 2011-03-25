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

package org.restlet.ext.oauth.protectedresource;

import org.restlet.data.Reference;

/**
 * Class for OAuth 2.0 protection of REST resources. Can only be deployed
 * together with the Authorization Server Restlet application. A Validation
 * resource must be started and mapped in the auth server.
 * 
 * Example invocation:
 * 
 * <pre>
 * {@code
 * public Restlet createInboundRoot(){
 *   ...
 *   LocalAuthorizer auth = new LocalAuthorizer(
 *              "/authorize",
 *              "http://localhost:8080/OAuth2Provider/validate");
 *   auth.setNext(ProtectedResource.class);
 *   router.attach("/me", auth);
 *   ...
 * }
 * }
 * </pre>
 * 
 * If you need to set up a resource that is protected through an external auth
 * server you need to use RemoteAuthorizer
 * 
 * @see org.restlet.ext.oauth.provider.ValidationServerResource
 * @see RemoteAuthorizer
 * 
 * @author Kristoffer Gronowski
 * 
 */
public class LocalAuthorizer extends RemoteAuthorizer {

    /**
     * @param validationURI
     *            validation url pointing to the auth server validation resource
     * @param authorizationURI
     *            url that should be invoked on errors
     */
    public LocalAuthorizer(String validationURI, String authorizationURI) {
        authorizeRef = new Reference(authorizationURI);
        validateRef = new Reference("riap://application" + validationURI);
    }
}