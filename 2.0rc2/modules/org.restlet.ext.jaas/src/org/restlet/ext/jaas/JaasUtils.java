/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.jaas;

import java.security.Principal;

import javax.security.auth.Subject;

import org.restlet.data.ClientInfo;
import org.restlet.security.Role;

/**
 * Utility class to facilitate integration between the Restlet and JAAS APIs.
 * 
 * @author Jerome Louvel
 */
public final class JaasUtils {

    /**
     * Creates a JAAS subject based on a given {@link ClientInfo}. It adds a
     * {@link ClientInfo#getUser()}, all the entries in
     * {@link ClientInfo#getRoles()} and all other principals in
     * {@link ClientInfo#getPrincipals()}.
     * 
     * @param clientInfo
     *            The client info to expose as a subject.
     * @return The populated JAAS subject.
     */
    public Subject createSubject(ClientInfo clientInfo) {
        Subject result = new Subject();

        if (clientInfo != null) {
            if (clientInfo.getUser() != null) {
                result.getPrincipals().add(clientInfo.getUser());
            }

            for (Role role : clientInfo.getRoles()) {
                result.getPrincipals().add(role);
            }

            for (Principal principal : clientInfo.getPrincipals()) {
                result.getPrincipals().add(principal);
            }
        }

        return result;
    }
}
