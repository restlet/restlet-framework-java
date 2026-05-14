/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import java.util.Arrays;
import java.util.Objects;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Representation;

/**
 * Describes a digest value and the digest algorithm used. Digests can have several use cases, such
 * as ensuring the integrity of representations exchanges between resources, or for authentication
 * purposes.
 *
 * @see Representation#getDigest()
 * @author Jerome Louvel
 */
public class Digest {

    /** Digest algorithm defined in RFC 1319. */
    public static final String ALGORITHM_MD2 = "MD2";

    /** Digest algorithm defined in RFC 1321. */
    public static final String ALGORITHM_MD5 = "MD5";

    /** No digest algorithm defined. */
    public static final String ALGORITHM_NONE = "NONE";

    /** Digest algorithm defined in Secure Hash Standard, NIST FIPS 180-1. */
    public static final String ALGORITHM_SHA_1 = "SHA-1";

    /** NIST approved digest algorithm from the SHA-2 family. */
    public static final String ALGORITHM_SHA_256 = "SHA-256";

    /** NIST approved digest algorithm from the SHA-2 family. */
    public static final String ALGORITHM_SHA_384 = "SHA-384";

    /** NIST approved digest algorithm from the SHA-2 family. */
    public static final String ALGORITHM_SHA_512 = "SHA-512";

    /**
     * Digest algorithm for the HTTP DIGEST scheme. This is exactly the A1 value specified in
     * RFC2617, which is a MD5 hash of the username, realm, and password, separated by a colon
     * character.
     */
    public static final String ALGORITHM_HTTP_DIGEST = "HTTP-DIGEST-A1";

    /** The digest algorithm. */
    private final String algorithm;

    /** The digest value. */
    private final byte[] value;

    /**
     * Constructor using the MD5 algorithm by default.
     *
     * @param value The digest value.
     */
    public Digest(byte[] value) {
        this(ALGORITHM_MD5, value);
    }

    /**
     * Constructor.
     *
     * @param algorithm The digest algorithm.
     * @param value The digest value.
     */
    public Digest(String algorithm, byte[] value) {
        this.algorithm = algorithm;

        // In Java 6, use Arrays.copyOf.
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Digest that)) {
            return false;
        }
        return Objects.equals(getAlgorithm(), that.getAlgorithm())
                && Arrays.equals(getValue(), that.getValue());
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
    public byte[] getValue() {
        // In Java 6, use Arrays.copyOf.
        byte[] result = new byte[this.value.length];
        System.arraycopy(this.value, 0, result, 0, this.value.length);

        return result;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(algorithm, value);
    }

    @Override
    public String toString() {
        return "Digest [algorithm=" + algorithm + ", value=" + Arrays.toString(value) + "]";
    }
}
