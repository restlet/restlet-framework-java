/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

import java.io.Serializable;
import java.security.Principal;

import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * Authentication response sent by client to an origin server.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ChallengeResponse {
    /**
     * Implementation of the Principal interface.
     * 
     * @author Stephan Koops
     */
    private final class PrincipalImpl implements Principal, Serializable {

        private static final long serialVersionUID = -1842197948591956691L;

        /**
         * Constructor for deserialization.
         */
        @SuppressWarnings("unused")
        private PrincipalImpl() {
        }

        @Override
        public boolean equals(Object another) {
            if (another == this)
                return true;
            if (!(another instanceof Principal))
                return false;
            Principal otherPrinc = (Principal) another;
            return getName().equals(otherPrinc.getName());
        }

        /**
         * Returns the name of this principal.
         * 
         * @return the name of this principal.
         */
        public String getName() {
            return getIdentifier();
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.Guard} or manually.
     */
    private volatile boolean authenticated;

    /** The raw credentials for custom challenge schemes. */
    private volatile String credentials;

    /** The user identifier, such as a login name or an access key. */
    private volatile String identifier;

    /** The additional scheme parameters. */
    private volatile Series<Parameter> parameters;

    /** The challenge scheme. */
    private volatile ChallengeScheme scheme;

    /** The user secret, such as a password or a secret key. */
    private volatile char[] secret;

    /**
     * Constructor.
     * 
     * @param scheme
     *                The challenge scheme.
     * @param credentials
     *                The raw credentials for custom challenge schemes.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String credentials) {
        this.scheme = scheme;
        this.credentials = credentials;
        this.identifier = null;
        this.secret = null;
        this.parameters = null;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *                The challenge scheme.
     * @param identifier
     *                The user identifier, such as a login name or an access
     *                key.
     * @param secret
     *                The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, char[] secret) {
        this.scheme = scheme;
        this.credentials = null;
        this.identifier = identifier;
        this.secret = secret;
        this.parameters = null;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *                The challenge scheme.
     * @param identifier
     *                The user identifier, such as a login name or an access
     *                key.
     * @param parameters
     *                The additional scheme parameters.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, Series<Parameter> parameters) {
        this.scheme = scheme;
        this.credentials = null;
        this.identifier = identifier;
        this.secret = null;
        this.parameters = parameters;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *                The challenge scheme.
     * @param identifier
     *                The user identifier, such as a login name or an access
     *                key.
     * @param secret
     *                The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, String secret) {
        this.scheme = scheme;
        this.credentials = null;
        this.identifier = identifier;
        this.secret = (secret != null) ? secret.toCharArray() : null;
        this.parameters = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a challenge request or is null don't evaluate
            // further
            if (obj instanceof ChallengeResponse) {
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
     * Returns the modifiable series of scheme parameters. Creates a new
     * instance if no one has been set.
     * 
     * @return The modifiable series of scheme parameters.
     */
    public Series<Parameter> getParameters() {
        if (this.parameters == null) {
            this.parameters = new Form();
        }

        return this.parameters;
    }

    /**
     * Gets the principal associated to the identifier property.
     * 
     * @return The {@link Principal}.
     */
    public Principal getPrincipal() {
        return new PrincipalImpl();
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
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.Guard} or manually.
     * 
     * @return True if the identifier or principal has been authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.Guard} or manually.
     * 
     * @param authenticated
     *                True if the identifier or principal has been
     *                authenticated.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Sets the credential components.
     * 
     * @param credentialComponents
     *                The credential components.
     */
    public void setCredentialComponents(Series<Parameter> credentialComponents) {
        this.parameters = credentialComponents;
    }

    /**
     * Sets the credentials.
     * 
     * @param credentials
     *                The credentials.
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the user identifier, such as a login name or an access key.
     * 
     * @param identifier
     *                The user identifier, such as a login name or an access
     *                key.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the scheme used.
     * 
     * @param scheme
     *                The scheme used.
     */
    public void setScheme(ChallengeScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *                The user secret, such as a password or a secret key.
     */
    public void setSecret(char[] secret) {
        this.secret = secret;
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *                The user secret, such as a password or a secret key.
     */
    public void setSecret(String secret) {
        this.secret = (secret == null) ? null : secret.toCharArray();
    }

}
