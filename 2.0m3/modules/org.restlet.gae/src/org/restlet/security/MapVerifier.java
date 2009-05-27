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

package org.restlet.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Verifier that stores its secrets in a map indexed by the identifier. Note
 * that this verifier isn't very secure by itself. It is recommended to use it
 * in conjunction with a {@link SecretDigestVerifier}.
 * 
 * @author Jerome Louvel
 */
public class MapVerifier extends LocalVerifier {

    /** The map of secrets. */
    private volatile ConcurrentMap<String, char[]> secrets;

    /**
     * Constructor.
     */
    public MapVerifier() {
        this(new ConcurrentHashMap<String, char[]>());
    }

    /**
     * Constructor.
     * 
     * @param secrets
     *            The map of secrets.
     */
    public MapVerifier(ConcurrentMap<String, char[]> secrets) {
        this.secrets = secrets;
    }

    @Override
    protected char[] getSecret(String identifier) {
        return (getSecrets() != null) ? getSecrets().get(identifier) : null;
    }

    /**
     * Returns the map of secrets.
     * 
     * @return The map of secrets.
     */
    public ConcurrentMap<String, char[]> getSecrets() {
        return secrets;
    }

    /**
     * Sets the map of secrets.
     * 
     * @param secrets
     *            The map of secrets.
     */
    public void setSecrets(ConcurrentMap<String, char[]> secrets) {
        this.secrets = secrets;
    }

}
