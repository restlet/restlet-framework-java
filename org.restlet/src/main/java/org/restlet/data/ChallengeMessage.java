/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.data;

import java.util.Objects;

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
        this(scheme, realm, parameters, Digest.ALGORITHM_MD5, null, null);
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
     * @param digestAlgorithm
     *            The digest algorithm.
     * @param opaque
     *            An opaque string of data which should be returned by the
     *            client unchanged.
     * @param serverNonce
     *            The server nonce.
     */
    public ChallengeMessage(ChallengeScheme scheme, String realm,
            Series<Parameter> parameters, String digestAlgorithm,
            String opaque, String serverNonce) {
        super();
        this.parameters = parameters;
        this.scheme = scheme;
        this.serverNonce = serverNonce;
        this.realm = realm;
        this.opaque = opaque;
        this.digestAlgorithm = digestAlgorithm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ChallengeMessage)) {
            return false;
        }

        final ChallengeMessage that = (ChallengeMessage) obj;

        return getParameters().equals(that.getParameters())
                && Objects.equals(getRealm(), that.getRealm())
                && Objects.equals(getScheme(), that.getScheme())
                && Objects.equals(getServerNonce(), that.getServerNonce())
                && Objects.equals(getOpaque(), that.getOpaque())
                && Objects.equals(getDigestAlgorithm(), that.getDigestAlgorithm());
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
            // [ifndef gwt] instruction
            this.parameters = new Series<Parameter>(Parameter.class);
            // [ifdef gwt] instruction uncomment
            // this.parameters = new org.restlet.engine.util.ParameterSeries();
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
