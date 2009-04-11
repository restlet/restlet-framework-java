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

package org.restlet.gae.security;

/**
 * Wrapper verifier that can handle digested secrets. If the input secret is a
 * digest, then the target secret must be a digest of the same algorithm.<br>
 * <br>
 * If the input secret is a regular secret, then the target secret can be in any
 * digest algorithm or a regular secret.
 * 
 * @author Jerome Louvel
 */
public class SecretDigestVerifier extends SecretVerifier {

    /** The digest algorithm of input secrets. */
    private String inputAlgorithm;

    /** The digest algorithm of output secrets. */
    private String outputAlgorithm;

    /**
     * Constructor.
     * 
     * @param inputAlgorithm
     *            The digest algorithm of input secrets.
     * @param outputAlgorithm
     *            The digest algorithm of output secrets.
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     */
    public SecretDigestVerifier(String inputAlgorithm, String outputAlgorithm,
            SecretVerifier wrappedVerifier) {
        this.inputAlgorithm = inputAlgorithm;
        this.outputAlgorithm = outputAlgorithm;
        checkCompatibility();
    }

    /**
     * Checks the compatibility of input and output algorithms. Throws an
     * illegal argument exception if necessary.
     */
    private void checkCompatibility() throws IllegalArgumentException {
        if ((this.inputAlgorithm != null)
                && !this.inputAlgorithm.equals(this.outputAlgorithm)) {
            throw new IllegalArgumentException(
                    "The input and output algorithms can't be different.");
        }
    }

    /**
     * Computes the digest of a secret according to a specified algorithm.
     * 
     * @param secret
     *            The regular secret to digest.
     * @param algorithm
     *            The digest algorithm to use.
     * @return The digested secret.
     */
    protected char[] digest(char[] secret, String algorithm) {
        // TODO
        return null;
    }

    /**
     * Returns the digest algorithm of input secrets.
     * 
     * @return The digest algorithm of input secrets.
     */
    public String getInputAlgorithm() {
        return inputAlgorithm;
    }

    /**
     * Returns the digest algorithm of output secrets.
     * 
     * @return The digest algorithm of output secrets.
     */
    public String getOutputAlgorithm() {
        return outputAlgorithm;
    }

    /**
     * Sets the digest algorithm of input secrets.
     * 
     * @param inputAlgorithm
     *            The digest algorithm of input secrets.
     */
    public void setInputAlgorithm(String inputAlgorithm) {
        this.inputAlgorithm = inputAlgorithm;
        checkCompatibility();
    }

    /**
     * Sets the digest algorithm of output secrets.
     * 
     * @param outputAlgorithm
     *            The digest algorithm of output secrets.
     */
    public void setOutputAlgorithm(String outputAlgorithm) {
        this.outputAlgorithm = outputAlgorithm;
        checkCompatibility();
    }

    @Override
    public boolean verify(String identifier, char[] inputSecret) {
        // char[] inputSecretDigest = inputSecret;

        if (getInputAlgorithm() == null) {
            if (getOutputAlgorithm() != null) {
                // inputSecretDigest = digest(inputSecret,
                // getOutputAlgorithm());
            } else {
                // The input secret should be a regular secret
            }
        } else {
            // The input secret is already digested
        }

        return true; // super.verify(subject, identifier, inputSecretDigest);
    }

}
