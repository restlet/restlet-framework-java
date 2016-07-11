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

package org.restlet.ext.crypto;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Digest;
import org.restlet.security.LocalVerifier;
import org.restlet.security.SecretVerifier;

/**
 * Wrapper verifier that can verify digested secrets. If the provided secret is
 * a digest, then the local secret must either be a digest of the same algorithm
 * or the wrapped verifier must be a {@link LocalVerifier} returning secrets in
 * clear.<br>
 * <br>
 * If the provided secret is a regular secret, then the local secret can be in
 * any digest algorithm or a regular secret.
 * 
 * @see Digest
 * @see DigestAuthenticator
 * @author Jerome Louvel
 */
public class DigestVerifier<T extends SecretVerifier> extends SecretVerifier {

    /** The digest algorithm of provided secrets. */
    private String algorithm;

    /** The digest algorithm of secrets returned by the wrapped verifier. */
    private String wrappedAlgorithm;

    /** The wrapped secret verifier. */
    private T wrappedVerifier;

    /**
     * Constructor.
     * 
     * @param algorithm
     *            The digest algorithm of provided secrets.
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     * @param wrappedAlgorithm
     *            The digest algorithm of secrets provided by the wrapped
     *            verifier.
     * @see Digest
     */
    public DigestVerifier(String algorithm, T wrappedVerifier,
            String wrappedAlgorithm) {
        this.algorithm = algorithm;
        this.wrappedAlgorithm = wrappedAlgorithm;
        this.wrappedVerifier = wrappedVerifier;
    }

    /**
     * Computes the digest of a secret according to a specified algorithm. By
     * default, MD5 hashes (represented as a sequence of 32 hexadecimal digits)
     * and SHA-1 hashes are supported. For additional algorithm, override this
     * method.
     * 
     * @param identifier
     *            The user identifier.
     * @param secret
     *            The regular secret to digest.
     * @param algorithm
     *            The digest algorithm to use.
     * @return The digested secret.
     * @see Digest
     */
    protected char[] digest(String identifier, char[] secret, String algorithm) {
        return DigestUtils.digest(secret, algorithm);
    }

    /**
     * Returns the digest algorithm of provided secrets. Provided secrets are
     * the ones sent by clients when attempting to authenticate.
     * 
     * @return The digest algorithm of input secrets.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the digest algorithm of secrets returned by the wrapped verifier.
     * The secrets from the wrapped verifier are the ones used by the verifier
     * to compare those sent by clients when attempting to authenticate.
     * 
     * @return The digest algorithm of secrets returned by the wrapped verifier.
     */
    public String getWrappedAlgorithm() {
        return wrappedAlgorithm;
    }

    /**
     * Returns the wrapped secret associated to a given identifier. This method
     * can only be called if the wrapped verifier is a {@link LocalVerifier}.
     * 
     * @param identifier
     *            The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    public char[] getWrappedSecret(String identifier) {
        char[] result = null;

        if (getWrappedVerifier() instanceof LocalVerifier) {
            LocalVerifier localVerifier = (LocalVerifier) getWrappedVerifier();
            result = localVerifier.getLocalSecret(identifier);
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "The wrapped verifier must be a LocalVerifier to allow digesting of wrapped secrets.");
        }

        return result;
    }

    /**
     * Returns the digest of the wrapped secret associated to a given
     * identifier. If the wrapped algorithm is null it returns the digest of the
     * wrapped secret, otherwise the algorithms must be identical. This method
     * can only be called if the wrapped verifier is a {@link LocalVerifier}.
     * 
     * @param identifier
     *            The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    public char[] getWrappedSecretDigest(String identifier) {
        char[] result = null;

        if (getWrappedAlgorithm() == null) {
            result = digest(identifier, getWrappedSecret(identifier),
                    getAlgorithm());
        } else if (getAlgorithm().equals(getWrappedAlgorithm())) {
            result = getWrappedSecret(identifier);
        } else {
            Context.getCurrentLogger().log(Level.WARNING,
                    "The digest algorithms can't be different.");
        }

        return result;
    }

    /**
     * Returns the wrapped secret verifier.
     * 
     * @return The wrapped secret verifier.
     */
    public T getWrappedVerifier() {
        return wrappedVerifier;
    }

    /**
     * Sets the digest algorithm of provided secrets. Provided secrets are the
     * ones sent by clients when attempting to authenticate.
     * 
     * @param algorithm
     *            The digest algorithm of secrets provided by the user.
     * @see Digest
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Sets the digest algorithm of secrets returned by the wrapped verifier.
     * The secrets from the wrapped verifier are the ones used by the verifier
     * to compare those sent by clients when attempting to authenticate.
     * 
     * @param wrappedAlgorithm
     *            The digest algorithm of secrets returned by the wrapped
     *            verifier.
     * @see Digest
     */
    public void setWrappedAlgorithm(String wrappedAlgorithm) {
        this.wrappedAlgorithm = wrappedAlgorithm;
    }

    /**
     * Sets the wrapped secret verifier.
     * 
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     */
    public void setWrappedVerifier(T wrappedVerifier) {
        this.wrappedVerifier = wrappedVerifier;
    }

    @Override
    public int verify(String identifier, char[] secret) {
        int result = RESULT_INVALID;
        char[] secretDigest = secret;

        if (getAlgorithm() == null) {
            if (getWrappedAlgorithm() != null) {
                secretDigest = digest(identifier, secret, getWrappedAlgorithm());
            } else {
                // Both secrets should be in clear
            }

            result = getWrappedVerifier().verify(identifier, secretDigest);
        } else {
            if (getWrappedAlgorithm() == null) {
                result = compare(secretDigest,
                        getWrappedSecretDigest(identifier)) ? RESULT_VALID
                        : RESULT_INVALID;
            } else if (getAlgorithm().equals(getWrappedAlgorithm())) {
                result = getWrappedVerifier().verify(identifier, secretDigest);
            } else {
                result = RESULT_UNSUPPORTED;
                Context.getCurrentLogger().log(Level.WARNING,
                        "The input and output algorithms can't be different.");
            }
        }

        return result;
    }
}
