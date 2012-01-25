/**
 * Copyright 2005-2012 Restlet S.A.S.
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

package org.restlet.engine.util;

import org.restlet.Request;
import org.restlet.data.Reference;
import org.restlet.engine.security.AuthenticatorUtils;

/**
 * Utilities related to URI references.
 * 
 * @author Jerome Louvel
 */
public class ReferenceUtils {

    /**
     * Returns the request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @param request
     *            The parent request.
     * @return The absolute request URI.
     */
    public static Reference update(Reference resourceRef, Request request) {
        Reference result = resourceRef.isAbsolute() ? resourceRef : resourceRef
                .getTargetRef();

        // Optionally update the request before formatting its URI
        result = AuthenticatorUtils.updateReference(result,
                request.getChallengeResponse(), request);

        return result;
    }

    /**
     * Returns the request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @param isProxied
     *            Indicates if the request goes through a proxy and requires an
     *            absolute URI.
     * @param request
     *            The parent request.
     * @return The absolute request URI.
     */
    public static String format(Reference resourceRef, boolean isProxied,
            Request request) {
        String result = null;
        Reference requestRef = update(resourceRef, request);

        if (isProxied) {
            result = requestRef.getIdentifier();
        } else {
            if (requestRef.hasQuery()) {
                result = requestRef.getPath() + "?" + requestRef.getQuery();
            } else {
                result = requestRef.getPath();
            }

            if ((result == null) || (result.equals(""))) {
                result = "/";
            }
        }

        return result;
    }

    /**
     * Constructor.
     */
    private ReferenceUtils() {
    }

}
