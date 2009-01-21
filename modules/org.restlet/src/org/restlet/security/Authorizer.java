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

import org.restlet.data.Request;

/**
 * Abstract class able to authorize Restlet requests.
 * 
 * @author Jerome Louvel
 */
public abstract class Authorizer {

    /** Authorizer returning true all the time. */
    public static final Authorizer ALWAYS = new Authorizer() {

        @Override
        public boolean authorize(Request request) {
            return true;
        }

    };

    /** Authorizer returning false all the time. */
    public static final Authorizer NEVER = new Authorizer() {

        @Override
        public boolean authorize(Request request) {
            return false;
        }

    };

    /**
     * Indicates if the request is authorized.
     * 
     * @param request
     *            The request to authorize.
     * @return True if the request is authorized.
     */
    public abstract boolean authorize(Request request);

}
