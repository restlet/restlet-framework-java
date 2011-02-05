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

package org.restlet.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Verifier that stores its local secrets in a map indexed by the identifier.
 * Note that this verifier isn't very secure by itself.
 * 
 * @author Jerome Louvel
 */
public class MapVerifier extends LocalVerifier {

    /** The map of local secrets. */
    private final ConcurrentMap<String, char[]> localSecrets;

    /**
     * Constructor.
     */
    public MapVerifier() {
        this(new ConcurrentHashMap<String, char[]>());
    }

    /**
     * Constructor.
     * 
     * @param localSecrets
     *            The map of local secrets.
     */
    public MapVerifier(ConcurrentMap<String, char[]> localSecrets) {
        this.localSecrets = localSecrets;
    }

    @Override
    public char[] getLocalSecret(String identifier) {
        return (identifier == null) ? null : getLocalSecrets().get(identifier);
    }

    /**
     * Returns the map of local secrets.
     * 
     * @return The map of local secrets.
     */
    public ConcurrentMap<String, char[]> getLocalSecrets() {
        return localSecrets;
    }

    /**
     * Sets the modifiable map of local secrets. This method clears the current
     * map and puts all entries in the parameter map.
     * 
     * @param localSecrets
     *            A map of local secrets.
     */
    public void setLocalSecrets(Map<String, char[]> localSecrets) {
        synchronized (getLocalSecrets()) {
            if (localSecrets != getLocalSecrets()) {
                getLocalSecrets().clear();

                if (localSecrets != null) {
                    getLocalSecrets().putAll(localSecrets);
                }
            }
        }
    }

}
