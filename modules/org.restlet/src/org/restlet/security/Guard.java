/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.security;

import org.restlet.Context;
import org.restlet.Filter;

/**
 * Filter guarding the access to an attached Restlet. More concretely, it guards
 * from unauthenticated and unauthorized requests, providing facilities to check
 * credentials such as passwords. It is also a relatively generic class which
 * can work with several authentication schemes such as HTTP Basic and HTTP
 * Digest.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part09">Tutorial:
 *      Guarding access to sensitive resources</a>
 * @author Jerome Louvel
 */
public class Guard extends Filter {

    private Authenticator authenticator;

    private Authorizer authorizer;

    /**
     * 
     * @param context
     * @param authenticator
     * @param authorizer
     */
    public Guard(Context context, Authenticator authenticator,
            Authorizer authorizer) {
        super(context);
        this.authenticator = authenticator;
        this.authorizer = authorizer;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public Authorizer getAuthorizer() {
        return authorizer;
    }

}
