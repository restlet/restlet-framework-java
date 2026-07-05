/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Digest}. */
class DigestTestCase {

    @Test
    void constructor_withValueOnly_usesMd5Algorithm() {
        Digest digest = new Digest(new byte[] {1, 2, 3});
        assertEquals(Digest.ALGORITHM_MD5, digest.getAlgorithm());
        assertArrayEquals(new byte[] {1, 2, 3}, digest.getValue());
    }

    @Test
    void constructor_withAlgorithm_setsAlgorithm() {
        Digest digest = new Digest(Digest.ALGORITHM_SHA_256, new byte[] {4, 5, 6});
        assertEquals(Digest.ALGORITHM_SHA_256, digest.getAlgorithm());
        assertArrayEquals(new byte[] {4, 5, 6}, digest.getValue());
    }

    @Test
    void getValue_returnsDefensiveCopy() {
        byte[] source = {1, 2, 3};
        Digest digest = new Digest(source);
        source[0] = 99;
        assertArrayEquals(new byte[] {1, 2, 3}, digest.getValue());

        byte[] returned = digest.getValue();
        returned[0] = 42;
        assertArrayEquals(new byte[] {1, 2, 3}, digest.getValue());
    }

    @Test
    void equals_sameAlgorithmAndValue_returnsTrue() {
        Digest digest1 = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});
        Digest digest2 = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});
        assertEquals(digest1, digest2);
        assertEquals(digest1.hashCode(), digest2.hashCode());
    }

    @Test
    void equals_differentAlgorithm_returnsFalse() {
        Digest digest1 = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});
        Digest digest2 = new Digest(Digest.ALGORITHM_SHA_1, new byte[] {1, 2, 3});
        assertNotEquals(digest1, digest2);
    }

    @Test
    void equals_differentValue_returnsFalse() {
        Digest digest1 = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});
        Digest digest2 = new Digest(Digest.ALGORITHM_MD5, new byte[] {9, 9, 9});
        assertNotEquals(digest1, digest2);
    }

    @Test
    void equals_differentType_returnsFalse() {
        Digest digest = new Digest(new byte[] {1});
        assertNotEquals(digest, "not a digest");
    }

    @Test
    void toString_containsAlgorithmAndValue() {
        Digest digest = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});
        String result = digest.toString();
        assertTrue(result.contains(Digest.ALGORITHM_MD5));
        assertTrue(result.contains("[1, 2, 3]"));
    }
}
