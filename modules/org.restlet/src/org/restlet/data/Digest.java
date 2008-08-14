/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

/**
 * Describes a representation digest to ensure its integrity when sent over the
 * network.
 * 
 * @author Jerome Louvel
 */
public class Digest {

    @Override
    public boolean equals(Object obj) {
        boolean result = (obj instanceof Digest) && obj != null;

        if (result) {
            Digest objDigest = (Digest) obj;
            result = (objDigest.getAlgorithm().equals(this.getAlgorithm()) && (objDigest
                    .getValue().equals(this.getValue())));
        }
        return result;
    }

    /** Digest algorightm defined in RFC 1319. */
    public static final String ALGORITHM_MD2 = "MD2";

    /** Digest algorightm defined in RFC 1321. */
    public static final String ALGORITHM_MD5 = "MD5";

    /** Digest algorightm defined in Secure Hash Standard, NIST FIPS 180-1. */
    public static final String ALGORITHM_SHA_1 = "SHA-1";

    /** NIST approved digest algorithm from SHA-2 family. */
    public static final String ALGORITHM_SHA_256 = "SHA-256";

    /** NIST approved digest algorithm from SHA-2 family. */
    public static final String ALGORITHM_SHA_384 = "SHA-384";

    /** NIST approved digest algorithm from SHA-2 family. */
    public static final String ALGORITHM_SHA_512 = "SHA-512";

    /** The digest algorithm. */
    private volatile String algorithm;

    /** The digest value. */
    private volatile String value;

    /**
     * Constructor using the MD5 algorithm by default.
     * 
     * @param value
     *            The digest value.
     */
    public Digest(String value) {
        this(ALGORITHM_MD5, value);
    }

    /**
     * Constructor.
     * 
     * @param algorithm
     *            The digest algorithm.
     * @param value
     *            The digest value.
     */
    public Digest(String algorithm, String value) {
        this.algorithm = algorithm;
        this.value = value;
    }

    /**
     * Returns the digest algorithm.
     * 
     * @return The digest algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the digest value.
     * 
     * @return The digest value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the digest algorithm.
     * 
     * @param algorithm
     *            The digest algorithm.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Sets the digest value.
     * 
     * @param value
     *            The digest value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
