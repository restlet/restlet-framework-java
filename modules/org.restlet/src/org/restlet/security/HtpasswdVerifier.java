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

import java.io.BufferedReader;
import java.io.IOException;

import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Response;

/**
 * Verifier based on digested password. Follows Apache HTTP format.
 * 
 * @author Remi Dewitte
 */
public class HtpasswdVerifier extends LocalVerifier {

    protected volatile Reference passwdFileRef;

    public HtpasswdVerifier() {

    }

    @Override
    public char[] getSecret(String login) {

        Response res = Application.getCurrent().getContext()
                .getClientDispatcher().get(passwdFileRef);
        if (res.getStatus().isSuccess() && res.getEntity() != null
                && res.getEntity().isAvailable()) {
            // Read the htpasswd file
            BufferedReader reader = null;
            String secret = null;
            try {
                reader = new BufferedReader(res.getEntity().getReader());

                String line;
                while (secret == null && (line = reader.readLine()) != null) {
                    int colonIndex = line.indexOf(':');
                    if (colonIndex != -1
                            && login.equals(line.substring(0, colonIndex)))
                        secret = line.substring(colonIndex + 1);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read " + passwdFileRef, e);
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not close "
                            + passwdFileRef, e);
                }
            }
            return secret.toCharArray();
        }
        return null;
    }

}
