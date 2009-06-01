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

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Digest;
import org.restlet.engine.util.DigestUtils;

/**
 * Wrapper verifier that can verify digested secrets. If the input secret is a
 * digest, then the output secret must either be a digest of the same algorithm
 * or the wrapped verifier must be a {@link LocalVerifier} returning secrets in
 * clear.<br>
 * <br>
 * If the input secret is a regular secret, then the output secret can be in any
 * digest algorithm or a regular secret.
 * 
 * @author Jerome Louvel
 */
public class SecretDigestVerifier extends SecretVerifier {

    /** The digest algorithm of input secrets. */
    private String inputAlgorithm;

    /** The digest algorithm of output secrets. */
    private String outputAlgorithm;

    /** The wrapped secret verifier. */
    private SecretVerifier wrappedVerifier;

    /**
     * Constructor.
     * 
     * @param inputAlgorithm
     *            The digest algorithm of input secrets.
     * @param outputAlgorithm
     *            The digest algorithm of output secrets.
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     * @see Digest
     */
    public SecretDigestVerifier(String inputAlgorithm, String outputAlgorithm,
            SecretVerifier wrappedVerifier) {
        this.inputAlgorithm = inputAlgorithm;
        this.outputAlgorithm = outputAlgorithm;
        this.wrappedVerifier = wrappedVerifier;
    }

    /**
     * Computes the digest of a secret according to a specified algorithm. By
     * default, MD5 hashes (represented as a sequence of 32 hexadecimal digits)
     * and SHA-1 hashes are supported. For additional algorithm, override this
     * method.
     * 
     * @param secret
     *            The regular secret to digest.
     * @param algorithm
     *            The digest algorithm to use.
     * @return The digested secret.
     * @see Digest
     */
    protected char[] digest(char[] secret, String algorithm) {
        return DigestUtils.digest(secret, algorithm);
    }

    /**
     * Returns the digest algorithm of input secrets. Input secrets are the ones
     * sent by clients when attempting to authenticate.
     * 
     * @return The digest algorithm of input secrets.
     */
    public String getInputAlgorithm() {
        return inputAlgorithm;
    }

    /**
     * Returns the digest algorithm of output secrets. Output secrets are the
     * ones used by the verifier to compare those sent by clients when
     * attempting to authenticate.
     * 
     * @return The digest algorithm of output secrets.
     */
    public String getOutputAlgorithm() {
        return outputAlgorithm;
    }

    /**
     * Returns the wrapped secret verifier.
     * 
     * @return The wrapped secret verifier.
     */
    public SecretVerifier getWrappedVerifier() {
        return wrappedVerifier;
    }

    /**
     * Sets the digest algorithm of input secrets. Input secrets are the ones
     * sent by clients when attempting to authenticate.
     * 
     * @param inputAlgorithm
     *            The digest algorithm of input secrets.
     * @see Digest
     */
    public void setInputAlgorithm(String inputAlgorithm) {
        this.inputAlgorithm = inputAlgorithm;
    }

    /**
     * Sets the digest algorithm of output secrets. Output secrets are the ones
     * used by the verifier to compare those sent by clients when attempting to
     * authenticate.
     * 
     * @param outputAlgorithm
     *            The digest algorithm of output secrets.
     * @see Digest
     */
    public void setOutputAlgorithm(String outputAlgorithm) {
        this.outputAlgorithm = outputAlgorithm;
    }

    /**
     * Sets the wrapped secret verifier.
     * 
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     */
    public void setWrappedVerifier(SecretVerifier wrappedVerifier) {
        this.wrappedVerifier = wrappedVerifier;
    }

    @Override
    public final boolean verify(String identifier, char[] inputSecret) {
        boolean result = false;
        char[] inputSecretDigest = inputSecret;

        if (getInputAlgorithm() == null) {
            if (getOutputAlgorithm() != null) {
                inputSecretDigest = digest(inputSecret, getOutputAlgorithm());
            } else {
                // Both secrets should be in clear
            }

            result = getWrappedVerifier().verify(identifier, inputSecretDigest);
        } else {
            if (getOutputAlgorithm() == null) {
                // Attempt to digest the output secret if we can get it in clear
                if (getWrappedVerifier() instanceof LocalVerifier) {
                    LocalVerifier localVerifier = (LocalVerifier) getWrappedVerifier();
                    result = compare(inputSecretDigest, digest(localVerifier
                            .getSecret(identifier), getInputAlgorithm()));
                } else {
                    Context
                            .getCurrentLogger()
                            .log(
                                    Level.WARNING,
                                    "The wrapped verifier must be a LocalVerifier to allow digesting of output secrets.");
                }
            } else {
                if (getInputAlgorithm().equals(getOutputAlgorithm())) {
                    // Same input and output algorithms
                    result = getWrappedVerifier().verify(identifier,
                            inputSecretDigest);
                } else {
                    // Different input and output algorithms
                    Context
                            .getCurrentLogger()
                            .log(Level.WARNING,
                                    "The input and output algorithms can't be different.");
                }
            }
        }

        return result;
    }
}
