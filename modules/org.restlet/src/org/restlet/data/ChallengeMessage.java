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

package org.restlet.data;

import org.restlet.engine.util.SystemUtils;
import org.restlet.util.Series;

/**
 * Base authentication challenge message exchanged between an origin server and
 * a client.
 * 
 * @author Jerome Louvel
 */
public abstract class ChallengeMessage {

    /** Authentication quality. */
    public static final String QUALITY_AUTHENTICATION = "auth";

    /** Authentication and integrity. */
    public static final String QUALITY_AUTHENTICATION_INTEGRITY = "auth-int";

    /** The raw value for custom challenge schemes. */
    private volatile String rawValue;

    /** The additional scheme parameters. */
    private volatile Series<Parameter> parameters;

    /** The challenge scheme. */
    private volatile ChallengeScheme scheme;

    /** The server nonce. */
    private volatile String serverNonce;

    /** The authentication realm. */
    private volatile String realm;

    /**
     * An opaque string of data which should be returned by the client
     * unchanged.
     */
    private volatile String opaque;

    /** The digest algorithm. */
    private volatile String digestAlgorithm;

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     */
    public ChallengeMessage(ChallengeScheme scheme) {
        this(scheme, null, null);
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param parameters
     *            The additional scheme parameters.
     */
    public ChallengeMessage(ChallengeScheme scheme, Series<Parameter> parameters) {
        this(scheme, null, null);
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeMessage(ChallengeScheme scheme, String realm) {
        this(scheme, realm, null);
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param realm
     *            The authentication realm.
     * @param parameters
     *            The additional scheme parameters.
     */
    public ChallengeMessage(ChallengeScheme scheme, String realm,
            Series<Parameter> parameters) {
        this.scheme = scheme;
        this.rawValue = null;
        this.realm = realm;
        this.parameters = parameters;
        this.digestAlgorithm = Digest.ALGORITHM_MD5;
        this.serverNonce = null;
        this.opaque = null;
    }

    /**
     * Returns the digest algorithm. See {@link Digest} class for DIGEST_*
     * constants. Default value is {@link Digest#ALGORITHM_MD5}.
     * 
     * @return The digest algorithm.
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * Returns an opaque string of data which should be returned by the client
     * unchanged.
     * 
     * @return An opaque string of data.
     */
    public String getOpaque() {
        return opaque;
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
     * Returns the raw challenge value.
     * 
     * @return The raw challenge value.
     */
    public String getRawValue() {
        return this.rawValue;
    }

    /**
     * Returns the realm name.
     * 
     * @return The realm name.
     */
    public String getRealm() {
        return this.realm;
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
     * Returns the server nonce.
     * 
     * @return The server nonce.
     */
    public String getServerNonce() {
        return serverNonce;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getScheme(), getRealm(), getParameters());
    }

    /**
     * Sets the digest algorithm. See {@link Digest} class for ALGORITHM_*
     * constants. Default value is {@link Digest#ALGORITHM_MD5}.
     * 
     * @param digestAlgorithm
     *            The digest algorithm.
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     * Sets an opaque string of data which should be returned by the client
     * unchanged.
     * 
     * @param opaque
     *            An opaque string of data.
     */
    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }

    /**
     * Sets the parameters.
     * 
     * @param parameters
     *            The parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the raw value.
     * 
     * @param rawValue
     *            The raw value.
     */
    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Sets the realm name.
     * 
     * @param realm
     *            The realm name.
     */
    public void setRealm(String realm) {
        this.realm = realm;
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
     * Sets the server nonce.
     * 
     * @param serverNonce
     *            The server nonce.
     */
    public void setServerNonce(String serverNonce) {
        this.serverNonce = serverNonce;
    }

}
