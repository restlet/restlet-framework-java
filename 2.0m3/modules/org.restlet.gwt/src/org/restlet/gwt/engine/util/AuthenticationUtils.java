/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.gwt.engine.util;

import org.restlet.gwt.data.ChallengeRequest;
import org.restlet.gwt.data.ChallengeScheme;

/**
 * Authentication utilities.
 * 
 * @author Jerome Louvel
 * @author Ray Waldin (ray@waldin.net)
 */
public class AuthenticationUtils {

    /**
     * Parses an authenticate header into a challenge request.
     * 
     * @param header
     *            The HTTP header value to parse.
     * @return The parsed challenge request.
     */
    public static ChallengeRequest parseAuthenticateHeader(String header) {
        ChallengeRequest result = null;

        if (header != null) {
            final int space = header.indexOf(' ');

            if (space != -1) {
                final String scheme = header.substring(0, space);
                result = new ChallengeRequest(new ChallengeScheme("HTTP_"
                        + scheme, scheme), null);
            } else {
                final String scheme = header.substring(0);
                result = new ChallengeRequest(new ChallengeScheme("HTTP_"
                        + scheme, scheme), null);
            }
        }

        return result;
    }

}
