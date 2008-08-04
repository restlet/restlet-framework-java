/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import org.restlet.util.Engine;

/**
 * Authentication response sent by client to an origin server.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ChallengeResponse {
    /** The challenge scheme. */
    private ChallengeScheme scheme;

    /** The user identifier, such as a login name or an access key. */
    private String identifier;

    /** The user secret, such as a password or a secret key. */
    private char[] secret;

    /** The raw credentials for custom challenge schemes. */
    private String credentials;

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param credentials
     *            The raw credentials for custom challenge schemes.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String credentials) {
        this.scheme = scheme;
        this.credentials = credentials;
        this.identifier = null;
        this.secret = null;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, String secret) {
        this.scheme = scheme;
        this.credentials = null;
        this.identifier = identifier;
        this.secret = (secret != null) ? secret.toCharArray() : null;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, char[] secret) {
        this.scheme = scheme;
        this.credentials = null;
        this.identifier = identifier;
        this.secret = secret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a challenge request or is null don't evaluate
            // further
            if ((obj instanceof ChallengeResponse) && obj != null) {
                ChallengeResponse that = (ChallengeResponse) obj;

                if (getCredentials() != null) {
                    result = getCredentials().equals(that.getCredentials());
                } else {
                    result = (that.getCredentials() == null);
                }

                if (result) {
                    if (getIdentifier() != null) {
                        result = getIdentifier().equals(that.getIdentifier());
                    } else {
                        result = (that.getIdentifier() == null);
                    }

                    if (result) {
                        if (getScheme() != null) {
                            result = getScheme().equals(that.getScheme());
                        } else {
                            result = (that.getScheme() == null);
                        }

                        if (result) {
                            if (getSecret() == null || that.getSecret() == null) {
                                // check if both are null
                                result = (getSecret() == that.getSecret());
                            } else {
                                if (getSecret().length == that.getSecret().length) {
                                    boolean equals = true;
                                    for (int i = 0; i < getSecret().length
                                            && equals; i++) {
                                        equals = (getSecret()[i] == that
                                                .getSecret()[i]);
                                    }
                                    result = equals;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the credentials.
     * 
     * @return The credentials.
     */
    public String getCredentials() {
        return this.credentials;
    }

    /**
     * Returns the user identifier, such as a login name or an access key.
     * 
     * @return The user identifier, such as a login name or an access key.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the scheme used.
     * 
     * @return The scheme used.
     */
    public ChallengeScheme getScheme() {
        return this.scheme;
    }

    /**
     * Returns the user secret, such as a password or a secret key.
     * 
     * @return The user secret, such as a password or a secret key.
     */
    public char[] getSecret() {
        return this.secret;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Engine.hashCode(getScheme(), getIdentifier(),
                (getSecret() == null) ? null : new String(getSecret()),
                getCredentials());
    }

    /**
     * Sets the credentials.
     * 
     * @param credentials
     *            The credentials.
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the user identifier, such as a login name or an access key.
     * 
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the scheme used.
     * 
     * @param scheme
     *            The scheme used.
     */
    public void setScheme(ChallengeScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setSecret(String secret) {
        this.secret = (secret == null) ? null : secret.toCharArray();
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setSecret(char[] secret) {
        this.secret = secret;
    }

}
